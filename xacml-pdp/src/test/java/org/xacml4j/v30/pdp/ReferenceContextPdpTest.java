package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.PreparedPolicySets;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pdp.NoCachePolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

import com.codahale.metrics.MetricRegistry;

/**
 * Cycle AP of change optimize-pdp-evaluation through a real decision point: in an unobserved decision a
 * reference and the policy or policy set it refers to share one context, and an observed decision keeps a
 * context for each. Both give the same response, with the policy identifier list of XACML 3.0 core, sections
 * 5.42 and 5.49, whether the references are pinned or resolved during the decision, and a cyclic reference
 * ends both alike.
 */
public class ReferenceContextPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final RequestContext REQUEST = RequestContext.builder().returnPolicyIdList(true).build();
	/** An observer that keeps nothing: the decisions it observes keep a context for each level */
	private static final DecisionObserver<String> OBSERVER = (pdp, request) -> "request";

	static Stream<Arguments> pins(){
		return Stream.of(Arguments.of(true), Arguments.of(false));
	}

	@ParameterizedTest(name = "pinned={0}")
	@MethodSource("pins")
	public void referencedPolicySetAndPolicyGiveTheResponseOfAnObservedDecision(boolean pinned)
	{
		VariableDefinition shared = new VariableDefinition("shared", StringVal.of("value"));
		Policy policy = Policy.builder("policy").version("1.2")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:deny-overrides"))
				.var(shared)
				.rule(Rule.builder("deny", Effect.DENY).condition(sharedEquals(shared, "other")))
				.rule(Rule.builder("permit", Effect.PERMIT).condition(sharedEquals(shared, "value"))
						.obligation(obligation("rule-obligation")).advice(advice("rule-advice")))
				.obligation(obligation("policy-obligation"))
				.build();
		PolicySet inner = set("inner", "2.0", PolicyIDReference.builder("policy").versionAsString("1.+").build())
				.obligation(obligation("inner-obligation"))
				.build();
		PolicySet root = set("root", "1.0", PolicySetIDReference.builder("inner").versionAsString("2.+").build()).build();
		InMemoryPolicyRepository repository = repository(policy, inner);

		Result unobserved = decide(pdp(root, repository, pinned, DecisionObserver.none()));
		Result observed = decide(pdp(root, repository, pinned, OBSERVER));

		assertEquals(Decision.PERMIT, unobserved.getDecision());
		assertThat(unobserved.getObligations().stream().map(Obligation::getId).toList())
				.containsExactly("rule-obligation", "policy-obligation", "inner-obligation");
		assertThat(unobserved.getAssociatedAdvice().stream().map(Advice::getId).toList()).containsExactly("rule-advice");
		assertThat(unobserved.getPolicyIdentifiers())
				.containsExactly(inner.getReference(), policy.getReference(), root.getReference()).inOrder();
		assertSameResponse(observed, unobserved);
	}

	static Stream<Arguments> cycles(){
		return Stream.of("two sets", "self", "deny overrides")
				.flatMap(shape -> Stream.of(false, true)
						.flatMap(matchFirst -> Stream.of(false, true)
								.flatMap(pinned -> Stream.of(false, true)
										.map(observed -> Arguments.of(shape, matchFirst, pinned, observed)))));
	}

	@ParameterizedTest(name = "{0}, matchFirst={1}, pinned={2}, observed={3}")
	@MethodSource("cycles")
	public void aCyclicReferenceEndsTheDecisionAsInAnObservedDecision(
			String shape, boolean matchFirst, boolean pinned, boolean observed)
	{
		String combining = matchFirst ? "only-one-applicable" : "first-applicable";
		PolicySet a;
		PolicySet root;
		InMemoryPolicyRepository repository;
		Decision expected;
		if(shape.equals("self")){
			a = set("a", "1.0", PolicySetIDReference.builder("a").build())
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:" + combining))
					.build();
			root = a;
			repository = repository(a);
			expected = Decision.INDETERMINATE;
		}else{
			a = set("a", "1.0", PolicySetIDReference.builder("b").build()).build();
			PolicySet b = set("b", "1.0", PolicySetIDReference.builder("a").build())
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:" + combining))
					.build();
			PolicySetIDReference reference = PolicySetIDReference.builder("a").build();
			if(shape.equals("deny overrides")){
				Policy permit = Policy.builder("permit")
						.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
						.rule(Rule.builder("permit-rule", Effect.PERMIT).obligation(obligation("permit-obligation")))
						.build();
				root = PolicySet.builder("root").version("1.0")
						.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "3.0:policy-combining-algorithm:deny-overrides"))
						.compositeDecisionRules(reference, permit).build();
				expected = Decision.INDETERMINATE_DP;
			}else{
				root = set("root", "1.0", reference).build();
				expected = Decision.INDETERMINATE;
			}
			repository = repository(a, b);
		}

		Result result = decide(pdp(root, repository, pinned, observed ? OBSERVER : DecisionObserver.none()));
		assertEquals(expected, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
		assertThat(result.getObligations()).isEmpty();
		assertThat(result.getAssociatedAdvice()).isEmpty();
		assertThat(result.getPolicyIdentifiers()).isEmpty();
	}

	static Stream<Arguments> backEdges(){
		return Stream.of("inline permit", "inline deny", "versions", "equal instances")
				.flatMap(shape -> Stream.of(false, true).flatMap(custom -> Stream.of(false, true)
						.flatMap(pinned -> Stream.of(false, true)
								.map(observed -> Arguments.of(shape, custom, pinned, observed)))));
	}

	@ParameterizedTest(name = "{0}, custom={1}, pinned={2}, observed={3}")
	@MethodSource("backEdges")
	public void aReferenceToAnActivePolicySetDoesNotReevaluateIt(String shape, boolean custom, boolean pinned,
			boolean observed){
		Effect effect = shape.equals("inline deny") ? Effect.DENY : Effect.PERMIT;
		AtomicReference<PolicySet> resolved = new AtomicReference<>();
		Rule.Builder rule = Rule.builder("leaf-rule", effect)
				.obligation(ObligationExpression.builder("leaf-obligation", effect));
		if(shape.equals("equal instances")){
			Expression currentSet = new Expression() {
				@Override
				public ValueExpTypeInfo getEvaluatesTo(){ return XacmlTypes.BOOLEAN; }

				@Override
				@SuppressWarnings("unchecked")
				public <T extends Expression> T evaluate(EvaluationContext context){
					return (T)BooleanVal.of(context.getCurrentPolicySet() == resolved.get());
				}

				@Override
				public void accept(ExpressionVisitor visitor){}
			};
			rule.condition(Condition.condition(currentSet));
		}
		Policy leaf = Policy.builder("leaf")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(rule)
				.build();
		PolicySetIDReference reference = shape.equals("versions")
				? PolicySetIDReference.builder("a").versionAsString("1.0").build()
				: PolicySetIDReference.builder("a").build();
		CompositeDecisionRule first = reference;
		if(shape.startsWith("inline")){
			String algorithm = effect == Effect.DENY ? "permit-unless-deny" : "first-applicable";
			String version = effect == Effect.DENY ? "3.0:" : "1.0:";
			first = PolicySet.builder("b").version("1.0")
					.withCombiningAlgorithm(policyAlgorithm(URN + version + "policy-combining-algorithm:" + algorithm, custom))
					.compositeDecisionRules(reference)
					.obligation(ObligationExpression.builder("inline-obligation", effect)).build();
		}
		String combining = effect == Effect.DENY ? "deny-overrides" : "permit-overrides";
		DecisionCombiningAlgorithm<CompositeDecisionRule> aAlgorithm =
				policyAlgorithm(URN + "3.0:policy-combining-algorithm:" + combining, custom);
		ObligationExpression aObligation = ObligationExpression.builder("a-obligation", effect).build();
		PolicySet a = PolicySet.builder("a").version("1.0")
				.withCombiningAlgorithm(aAlgorithm)
				.compositeDecisionRules(first, leaf)
				.obligation(aObligation).build();
		PolicySet root = a;
		PolicySet stored = a;
		if(shape.equals("versions")){
			root = set("root", "1.0", PolicySetIDReference.builder("a").versionAsString("1.*").build()).build();
		}else if(shape.equals("equal instances")){
			stored = PolicySet.builder("a").version("1.0")
					.withCombiningAlgorithm(aAlgorithm)
					.compositeDecisionRules(first, leaf)
					.obligation(aObligation).build();
			assertEquals(a, stored);
			resolved.set(stored);
		}

		Result result = decide(pdp(root, repository(stored), pinned, observed ? OBSERVER : DecisionObserver.none()));
		assertEquals(effect == Effect.DENY ? Decision.DENY : Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertThat(result.getObligations().stream().map(Obligation::getId).toList())
				.containsExactly("leaf-obligation", "a-obligation");
	}

	static Stream<Arguments> pinAndObservation(){
		return Stream.of(false, true).flatMap(pinned -> Stream.of(false, true)
				.map(observed -> Arguments.of(pinned, observed)));
	}

	@ParameterizedTest(name = "pinned={0}, observed={1}")
	@MethodSource("pinAndObservation")
	public void aRootSelfReferenceFailsBeforeMatchingItsSibling(boolean pinned, boolean observed){
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("missing").category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING).build();
		AttributeDesignator designator = AttributeDesignator.builder().key(key).mustBePresent(true).build();
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("value"), designator,
						FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow()).build()).build()).build();
		Policy sibling = Policy.builder("sibling").target(target)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("permit", Effect.PERMIT)).build();
		PolicySet root = PolicySet.builder("a").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:only-one-applicable"))
				.compositeDecisionRules(PolicySetIDReference.builder("a").build(), sibling).build();
		Result result = decide(pdp(root, repository(root), pinned, observed ? OBSERVER : DecisionObserver.none()));

		assertEquals(Decision.INDETERMINATE, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
		assertThat(result.getObligations()).isEmpty();
	}

	@Test
	public void aCyclicPolicyReferenceReturnsIndeterminateFromBothEntryPoints(){
		PolicyIDReference ancestor = PolicyIDReference.builder("policy").build();
		PolicyIDReference reference = PolicyIDReference.builder("policy").build();
		EvaluationContext parent = mock(EvaluationContext.class);
		when(parent.getCurrentPolicyIDReference()).thenReturn(ancestor);
		EvaluationContext cycle = reference.createContext(parent);

		assertEquals(Decision.INDETERMINATE, reference.evaluate(cycle));
		assertEquals(MatchResult.INDETERMINATE, reference.isMatch(cycle));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR,
				cycle.getEvaluationStatus().orElseThrow().getStatusCode().getValue());
	}

	@Test
	public void reusingAnEnteredPolicySetReferenceContextRemainsApplicable() throws Exception {
		Policy permit = Policy.builder("permit")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("permit-rule", Effect.PERMIT)).build();
		PolicySet target = set("target", "1.0", permit).build();
		PolicySetIDReference reference = PolicySetIDReference.builder("target").build();
		EvaluationContext parent = mock(EvaluationContext.class);
		when(parent.resolve(reference)).thenReturn(target);
		EvaluationContext context = reference.createContext(parent);

		assertEquals(Decision.PERMIT, reference.evaluate(reference.createContext(context)));
	}

	@Test
	public void reusingAnEnteredPolicyReferenceContextRemainsApplicable() throws Exception {
		Policy target = Policy.builder("target")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("permit-rule", Effect.PERMIT)).build();
		PolicyIDReference reference = PolicyIDReference.builder("target").build();
		EvaluationContext parent = mock(EvaluationContext.class);
		when(parent.resolve(reference)).thenReturn(target);
		EvaluationContext context = reference.createContext(parent);

		assertEquals(Decision.PERMIT, reference.evaluate(reference.createContext(context)));
	}

	static Stream<Arguments> policyEntries(){
		return Stream.of(false, true).flatMap(matchFirst -> Stream.of(false, true)
				.flatMap(pinned -> Stream.of(false, true)
						.map(observed -> Arguments.of(matchFirst, pinned, observed))));
	}

	@ParameterizedTest(name = "matchFirst={0}, pinned={1}, observed={2}")
	@MethodSource("policyEntries")
	public void aPolicyReferenceReenteredInsideItsPolicyReturnsAProcessingError(
			boolean matchFirst, boolean pinned, boolean observed){
		PolicyIDReference reference = PolicyIDReference.builder("policy").build();
		DecisionCombiningAlgorithm<Rule> algorithm = new DecisionCombiningAlgorithm<>() {
			@Override
			public String getId(){ return "urn:test:policy-reference-inside-policy"; }

			@Override
			public Decision combine(EvaluationContext context, List<Rule> rules){
				EvaluationContext nested = reference.createContext(context);
				if(matchFirst){
					return reference.isMatch(nested) == MatchResult.INDETERMINATE
							? Decision.INDETERMINATE : Decision.PERMIT;
				}
				return reference.evaluate(nested);
			}
		};
		Policy policy = Policy.builder("policy").combiningAlgorithm(algorithm).build();
		PolicySet root = set("root", "1.0", reference).build();
		DecisionCombiningAlgorithmProvider provider = mock(DecisionCombiningAlgorithmProvider.class);
		when(provider.isRuleAlgorithmProvided(algorithm.getId())).thenReturn(true);
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("references", FUNCTIONS, provider);
		repository.add(policy);
		Result result = decide(pdp(root, repository, pinned, observed ? OBSERVER : DecisionObserver.none()));

		assertEquals(Decision.INDETERMINATE, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
	}

	@Test
	public void aPolicyReferenceInsideAPolicyReturnsASyntaxError(){
		Policy policy = Policy.builder("policy")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.build();
		PolicyIDReference reference = PolicyIDReference.builder("policy").build();
		EvaluationContext parent = mock(EvaluationContext.class);
		when(parent.getCurrentPolicy()).thenReturn(policy);
		EvaluationContext invalid = reference.createContext(parent);

		assertEquals(Decision.INDETERMINATE, reference.evaluate(invalid));
		assertEquals(MatchResult.INDETERMINATE, reference.isMatch(invalid));
		assertEquals(StatusCodeId.SYNTAX_ERROR,
				invalid.getEvaluationStatus().orElseThrow().getStatusCode().getValue());
	}

	@ParameterizedTest(name = "pinned={0}, observed={1}")
	@MethodSource("pinAndObservation")
	public void aSeparateEqualPolicyIsInvalidInsideAPolicyRatherThanCyclic(boolean pinned, boolean observed){
		PolicyIDReference reference = PolicyIDReference.builder("policy").build();
		DecisionCombiningAlgorithm<Rule> algorithm = new DecisionCombiningAlgorithm<>() {
			@Override
			public String getId(){ return "urn:test:equal-policy-reference"; }

			@Override
			public Decision combine(EvaluationContext context, List<Rule> rules){
				return reference.evaluate(reference.createContext(context));
			}
		};
		Policy active = Policy.builder("policy").combiningAlgorithm(algorithm).build();
		Policy target = Policy.builder("policy").combiningAlgorithm(algorithm).build();
		assertEquals(active, target);
		DecisionCombiningAlgorithmProvider provider = mock(DecisionCombiningAlgorithmProvider.class);
		when(provider.isRuleAlgorithmProvided(algorithm.getId())).thenReturn(true);
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("references", FUNCTIONS, provider);
		repository.add(target);
		PolicySet root = set("root", "1.0", active).build();
		Result result = decide(pdp(root, repository, pinned, observed ? OBSERVER : DecisionObserver.none()));

		assertEquals(Decision.INDETERMINATE, result.getDecision());
		assertEquals(StatusCodeId.SYNTAX_ERROR, result.getStatus().getStatusCode().getValue());
	}

	private static PolicyDecisionPoint pdp(PolicySet root, InMemoryPolicyRepository repository, boolean pinned,
			DecisionObserver<?> observer){
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		RequestContextHandlerChain handlers = new RequestContextHandlerChain(List.of());
		PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory factory = pinned
				? new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
						new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(),
						XPathProvider.defaultProvider(), pip, handlers, PreparedPolicySets.create(false))
				: new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
						new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(),
						XPathProvider.defaultProvider(), pip, handlers);
		return new DefaultPolicyDecisionPoint("references", new MetricRegistry(), factory, observer);
	}

	private static Result decide(PolicyDecisionPoint pdp){
		return pdp.decide(REQUEST).getResults().iterator().next();
	}

	private static InMemoryPolicyRepository repository(CompositeDecisionRule... rules){
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("references", FUNCTIONS, ALGORITHMS);
		for(CompositeDecisionRule rule : rules){
			repository.add(rule);
		}
		return repository;
	}

	/**
	 * The built-in policy combining algorithm of an id or, when custom, one of another class that combines as it
	 * does: a policy set with it stages its responses in a wrapper context instead of the root of the decision
	 */
	private static DecisionCombiningAlgorithm<CompositeDecisionRule> policyAlgorithm(String id, boolean custom){
		DecisionCombiningAlgorithm<CompositeDecisionRule> builtIn = ALGORITHMS.getPolicyAlgorithm(id);
		if(!custom){
			return builtIn;
		}
		return new DecisionCombiningAlgorithm<>() {
			@Override
			public String getId(){ return id; }

			@Override
			public Decision combine(EvaluationContext context, List<CompositeDecisionRule> decisions){
				return builtIn.combine(context, decisions);
			}
		};
	}

	private static PolicySet.Builder set(String id, String version, CompositeDecisionRule child){
		return PolicySet.builder(id).version(version)
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:first-applicable"))
				.compositeDecisionRules(child);
	}

	private static Condition sharedEquals(VariableDefinition shared, String value){
		return Condition.condition(Apply.builder(URN + "1.0:function:string-equal", FUNCTIONS::getFunction)
				.param(new VariableReference(shared), StringVal.of(value)).build());
	}

	private static ObligationExpression.Builder obligation(String id){
		return ObligationExpression.builder(id, Effect.PERMIT).attribute("origin", StringVal.of(id));
	}

	private static AdviceExpression.Builder advice(String id){
		return AdviceExpression.builder(id, Effect.PERMIT).attribute("origin", StringVal.of(id));
	}

	/** The results, their status errors, and the order of their responses and policy identifiers */
	private static void assertSameResponse(Result expected, Result actual){
		assertEquals(expected, actual);
		assertEquals(errorChain(expected), errorChain(actual));
		assertEquals(List.copyOf(expected.getObligations()), List.copyOf(actual.getObligations()));
		assertEquals(List.copyOf(expected.getAssociatedAdvice()), List.copyOf(actual.getAssociatedAdvice()));
		assertEquals(List.copyOf(expected.getPolicyIdentifiers()), List.copyOf(actual.getPolicyIdentifiers()));
	}

	private static List<String> errorChain(Result result){
		List<String> chain = new ArrayList<>();
		for(Throwable cause = result.getStatus().getError().orElse(null); cause != null; cause = cause.getCause()){
			chain.add(cause.getClass().getName() + ":" + cause.getMessage());
		}
		return chain;
	}
}
