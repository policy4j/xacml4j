package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Bead policy4j-the.4.54, scenario 3 through the PDP and a real policy
 * repository: a PolicyIdReference and a PolicySetIdReference below a nested
 * policy set are resolved from the repository, and the response carries
 * the obligations and advice of every level exactly once.
 */
class NestedResponseStagingPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final String RESOURCE_ID = "urn:xacml4j:test:resource-id";

	@Test
	void silentAndRespondingNestedPoliciesKeepResponseOrderAcrossPreparedDecisions(){
		PolicyRepository repository = PolicyRepository.build("ordered-staging").build();
		repository.add(orderedPolicy("b"));
		PolicySet nested = PolicySet.builder("nested-ordered").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policy(silentPolicy("silent", Effect.PERMIT))
				.policy(orderedPolicy("a"))
				.compositeDecisionRules(PolicyIDReference.builder("b").build())
				.build();
		PolicySet root = PolicySet.builder("root-ordered").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policySet(nested).policy(orderedPolicy("c"))
				.obligation(obligation("d", "root"))
				.advice(AdviceExpression.builder("d", Effect.PERMIT).attribute("origin", StringVal.of("root")))
				.build();
		PolicyDecisionPoint pdp = pdp(root, repository);
		for(int i = 0; i < 2; i++){
			Result result = decide(pdp);
			assertEquals(Decision.PERMIT, result.getDecision());
			assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
			assertEquals(List.of("a", "b", "c", "d"), orderedIds(result.getObligations()));
			assertEquals(List.of("a", "b", "c", "d"), orderedIds(result.getAssociatedAdvice()));
		}
	}

	@Test
	void losingRuleResponsesAreFilteredThroughNestedSets(){
		Policy mixed = Policy.builder("mixed").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "3.0:rule-combining-algorithm:permit-overrides"))
				.rule(Rule.builder("deny-rule", Effect.DENY)
						.obligation(ObligationExpression.builder("losing-obligation", Effect.DENY))
						.advice(AdviceExpression.builder("losing-advice", Effect.DENY)))
				.rule(Rule.builder("permit-rule", Effect.PERMIT)).build();
		PolicySet nested = PolicySet.builder("mixed-set").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(mixed).build();
		Policy winner = Policy.builder("winner").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("winner-rule", Effect.DENY))
				.obligation(ObligationExpression.builder("winner-obligation", Effect.DENY))
				.advice(AdviceExpression.builder("winner-advice", Effect.DENY)).build();
		PolicySet root = PolicySet.builder("root-mixed").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policySet(nested).policy(winner).build();
		Result result = decide(pdp(root, PolicyRepository.build("mixed-staging").build()));
		assertEquals(Decision.DENY, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("winner-obligation"), orderedIds(result.getObligations()));
		assertEquals(List.of("winner-advice"), orderedIds(result.getAssociatedAdvice()));
	}

	/**
	 * The responses a custom combining algorithm adds through its context, itself or from a worker
	 * it joins, go with its own decision only (review R-AN, Codex Sol, P2-1)
	 */
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void customCombinerResponsesAlsoFromAJoinedWorkerRemainScopedToTheCombinerDecision(boolean policySet){
		CompositeDecisionRule custom = policySet
				? PolicySet.builder("custom-set").version("1.0").withCombiningAlgorithm(customCombiner())
						.policy(silentPolicy("custom-child", Effect.DENY)).build()
				: Policy.builder("custom-policy").version("1.0").combiningAlgorithm(customCombiner())
						.rule(Rule.builder("custom-rule", Effect.DENY)).build();
		PolicySet root = PolicySet.builder("custom-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.compositeDecisionRules(custom).policy(silentPolicy("winner", Effect.PERMIT)).build();
		Result result = decide(pdp(root, PolicyRepository.build("custom-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of(), orderedIds(result.getObligations()));
		assertEquals(List.of(), orderedIds(result.getAssociatedAdvice()));
	}

	/**
	 * An attribute resolver writes to the root of the decision, which no staging holds (design
	 * D36): its responses stay whatever the decision of the policy whose condition resolved the
	 * attribute (review R-AN, Codex Sol, P2-1)
	 */
	@Test
	void responsesWrittenToTheRootByAResolverAreNotStaged(){
		AttributeDesignator role = AttributeDesignator.builder().key(AttributeDesignatorKey.builder("role")
				.category(CategoryId.SUBJECT_ACCESS).dataType(XacmlTypes.STRING).build()).mustBePresent(true).build();
		PolicyInformationPoint pip = PolicyInformationPoint.builder("resolver-staging").registry(ResolverRegistry.builder()
				.withAttributeResolver(AttributeResolverDescriptor.builder("writer", "writer", CategoryId.SUBJECT_ACCESS)
						.noCache().attribute("role", XacmlTypes.STRING)
						.build(c -> {
							addPermitResponses(c.getEvaluationContext(), "resolver");
							return Map.of("role", XacmlTypes.STRING.bagOf(StringVal.of("admin")));
						}))
				.build()).build();
		FunctionSpec stringEqual = FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow();
		FunctionSpec oneAndOnly = FUNCTIONS.getFunction(URN + "1.0:function:string-one-and-only").orElseThrow();
		// The Deny rule has an obligation, so its policy stages (design D36)
		Policy denier = Policy.builder("denier").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("denier-rule", Effect.DENY)
						.condition(Condition.condition(Apply.builder(stringEqual)
								.param(Apply.builder(oneAndOnly).param(role).build(), StringVal.of("admin")).build()))
						.obligation(ObligationExpression.builder("denier-obligation", Effect.DENY)))
				.build();
		PolicySet root = PolicySet.builder("resolver-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.policy(denier).policy(orderedPolicy("a")).build();
		Result result = decide(PolicyDecisionPointBuilder.builder("resolver-staging")
				.policyRepository(PolicyRepository.build("resolver-staging").build()).pip(pip).rootPolicy(root).build());
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("a", "resolver-obligation"), ids(result.getObligations()));
		assertEquals(List.of("a", "resolver-advice"), ids(result.getAssociatedAdvice()));
	}

	/**
	 * The extended Indeterminate pass of a policy whose target is Indeterminate (XACML 3.0, sections
	 * 7.13 and 7.14) ends its own staging only: the enclosing set is Permit with its own obligations
	 * and advice, and the policy's are not returned (section 7.18; review R-AN2, Codex Sol, P2-1)
	 */
	@Test
	void anEnclosingSetKeepsItsResponsesAfterTheExtendedIndeterminatePassOfAChild(){
		Policy indeterminate = Policy.builder("indeterminate").version("1.0")
				.target(missingAttributeTarget())
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("indeterminate-rule", Effect.PERMIT)
						.obligation(obligation("indeterminate-rule", "indeterminate-rule"))
						.advice(AdviceExpression.builder("indeterminate-rule", Effect.PERMIT)
								.attribute("origin", StringVal.of("indeterminate-rule"))))
				.obligation(obligation("indeterminate", "indeterminate"))
				.advice(AdviceExpression.builder("indeterminate", Effect.PERMIT).attribute("origin", StringVal.of("indeterminate")))
				.build();
		PolicySet enclosed = PolicySet.builder("enclosed").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-unless-deny"))
				.policy(indeterminate)
				.obligation(obligation("enclosed", "enclosed"))
				.advice(AdviceExpression.builder("enclosed", Effect.PERMIT).attribute("origin", StringVal.of("enclosed")))
				.build();
		PolicySet root = PolicySet.builder("pass-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policySet(enclosed).build();
		Result result = decide(pdp(root, PolicyRepository.build("pass-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("enclosed"), orderedIds(result.getObligations()));
		assertEquals(List.of("enclosed"), orderedIds(result.getAssociatedAdvice()));
	}

	/**
	 * A worker that the condition of a Deny rule starts and joins adds Permit responses through the
	 * context of the enclosing policy set while the rule's policy stages: they wait with that set,
	 * which is Deny, so the Permit of a later sibling does not return them (XACML 3.0, section 7.18;
	 * review R-AN2, Codex Sol, P2-2)
	 */
	@Test
	void responsesAJoinedWorkerAddsThroughAnEnclosingSetGoWithTheDecisionOfThatSet(){
		Expression condition = new Expression() {
			@Override public ValueExpTypeInfo getEvaluatesTo(){ return XacmlTypes.BOOLEAN; }
			@Override @SuppressWarnings("unchecked") public <T extends Expression> T evaluate(EvaluationContext context){
				EvaluationContext set = context;
				while(set.getCurrentPolicy() != null){
					set = set.getParentContext();
				}
				EvaluationContext enclosing = set;
				onJoinedWorker(() -> addPermitResponses(enclosing, "worker"));
				return (T)BooleanVal.of(true);
			}
			@Override public void accept(ExpressionVisitor visitor){ }
		};
		Policy denier = Policy.builder("denier").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("denier-rule", Effect.DENY).condition(Condition.condition(condition)))
				.build();
		PolicySet enclosed = PolicySet.builder("enclosed").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policy(denier).build();
		PolicySet root = PolicySet.builder("worker-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.policySet(enclosed).policy(orderedPolicy("a")).build();
		Result result = decide(pdp(root, PolicyRepository.build("worker-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("a"), orderedIds(result.getObligations()));
		assertEquals(List.of("a"), orderedIds(result.getAssociatedAdvice()));
	}

	@Test
	void responsesAddedByAConditionAreFilteredWithItsPolicy(){
		Expression expression = new Expression() {
			@Override public ValueExpTypeInfo getEvaluatesTo(){ return XacmlTypes.BOOLEAN; }
			@Override @SuppressWarnings("unchecked") public <T extends Expression> T evaluate(EvaluationContext context){
				context.addObligations(Decision.PERMIT, List.of(Obligation.builder("condition-obligation", Effect.PERMIT).build()));
				context.addAdvices(Decision.PERMIT, List.of(Advice.builder("condition-advice", Effect.PERMIT).build()));
				return (T)BooleanVal.of(true);
			}
			@Override public void accept(ExpressionVisitor visitor){ }
		};
		Policy child = Policy.builder("conditional").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("conditional-rule", Effect.DENY).condition(Condition.condition(expression)))
				.build();
		PolicySet root = PolicySet.builder("conditional-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.policy(child).policy(silentPolicy("winner", Effect.PERMIT)).build();
		Result result = decide(pdp(root, PolicyRepository.build("conditional-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of(), orderedIds(result.getObligations()));
		assertEquals(List.of(), orderedIds(result.getAssociatedAdvice()));
	}

	@Test
	void silentTargetsAndConditionsLeaveTheResponsesOfASiblingUnchanged(){
		FunctionSpec stringEqual = FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow();
		Policy silent = Policy.builder("silent").version("1.0")
				.target(Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
						.match(StringVal.of("doc"), resourceId(), stringEqual).build()).build()).build())
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("silent-rule", Effect.PERMIT).condition(Condition.condition(
						Apply.builder(stringEqual).param(StringVal.of("a"), StringVal.of("a")).build())))
				.build();
		PolicySet nested = PolicySet.builder("silent-set").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(silent).build();
		PolicySet root = PolicySet.builder("silent-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policySet(nested).policy(orderedPolicy("a")).build();
		RequestContext request = RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE).entity(Entity.builder()
						.attribute(Attribute.builder(RESOURCE_ID).value(XacmlTypes.STRING.ofAny("doc")).build())
						.build()).build())
				.build();
		Result result = pdp(root, PolicyRepository.build("silent-staging").build())
				.decide(request).getResults().iterator().next();
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("a"), orderedIds(result.getObligations()));
		assertEquals(List.of("a"), orderedIds(result.getAssociatedAdvice()));
	}

	@Test
	void responsesWrittenByAFunctionAreFilteredWithItsPolicy(){
		FunctionSpec writes = FunctionSpecBuilder.builder("urn:xacml4j:test:writes-responses")
				.build(XacmlTypes.BOOLEAN, new FunctionInvocation(){
					@Override
					public ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
						return invoke(spec, context, List.of(arguments));
					}

					@Override
					public ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
						context.addObligations(Decision.PERMIT, List.of(Obligation.builder("function-obligation", Effect.PERMIT).build()));
						context.addAdvices(Decision.PERMIT, List.of(Advice.builder("function-advice", Effect.PERMIT).build()));
						return BooleanVal.of(true);
					}
				});
		Policy child = Policy.builder("function-conditional").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("function-rule", Effect.DENY).condition(Condition.condition(Apply.builder(writes).build())))
				.build();
		PolicySet root = PolicySet.builder("function-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.policy(child).policy(silentPolicy("winner", Effect.PERMIT)).build();
		Result result = decide(pdp(root, PolicyRepository.build("function-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of(), orderedIds(result.getObligations()));
		assertEquals(List.of(), orderedIds(result.getAssociatedAdvice()));
	}

	/** Review R-AA (Codex Astra) P1-1: a definition of another class can write responses whatever its expression */
	@Test
	void responsesWrittenByAVariableDefinitionAreFilteredWithItsPolicy(){
		VariableDefinition writes = new VariableDefinition("writes", BooleanVal.of(true)){
			@Override
			public ValueExp evaluate(EvaluationContext context){
				addPermitResponses(context, "definition");
				return super.evaluate(context);
			}
		};
		Result result = decide(pdp(denyBesideAPermit(Condition.condition(new VariableReference(writes))),
				PolicyRepository.build("definition-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(List.of(), orderedIds(result.getObligations()));
		assertEquals(List.of(), orderedIds(result.getAssociatedAdvice()));
	}

	/**
	 * Review R-AA (Codex Astra) P1-2: a standard function from a custom invocation factory gets the evaluation
	 * context and can write responses
	 */
	@Test
	void responsesWrittenThroughACustomInvocationFactoryAreFilteredWithItsPolicy(){
		FunctionProvider functions = FunctionProviderBuilder.builder(new FunctionInvocationFactory(){
			@Override
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m){
				PolicyToPlatformFunctionInvocation<T> standard = FunctionInvocationFactory.defaultFactory().create(instance, m);
				return params -> {
					if(params.length > 0 && params[0] instanceof EvaluationContext context){
						addPermitResponses(context, "factory");
					}
					return standard.invoke(params);
				};
			}
		}).build();
		FunctionSpec and = functions.getFunction(URN + "1.0:function:and").orElseThrow();
		Result result = decide(pdp(denyBesideAPermit(Condition.condition(Apply.builder(and).param(BooleanVal.of(true)).build())),
				PolicyRepository.build("factory-staging").build()));
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(List.of(), orderedIds(result.getObligations()));
		assertEquals(List.of(), orderedIds(result.getAssociatedAdvice()));
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void sharedVariableDagKeepsNestedResponseOrderAcrossDecisions(boolean targetIndex){
		withoutDebugLog(() -> {
			List<VariableDefinition> variables = sharedVariableDag(new VariableDefinition("level-0", BooleanVal.of(true)), 16);
			Policy silent = Policy.builder("dag-silent").version("1.0")
					.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
							URN + "1.0:rule-combining-algorithm:first-applicable"))
					.vars(variables)
					.rule(Rule.builder("dag-rule", Effect.PERMIT)
							.condition(Condition.condition(new VariableReference(variables.getLast())))).build();
			PolicySet nested = PolicySet.builder("dag-nested").version("1.0")
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
							URN + "3.0:policy-combining-algorithm:deny-overrides"))
					.policy(silent).policy(orderedPolicy("a"))
					.obligation(obligation("b", "b"))
					.advice(AdviceExpression.builder("b", Effect.PERMIT).attribute("origin", StringVal.of("b")))
					.build();
			PolicySet root = PolicySet.builder("dag-root").version("1.0")
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
							URN + "3.0:policy-combining-algorithm:deny-overrides"))
					.policySet(nested).policy(orderedPolicy("c")).build();
			PolicyDecisionPoint pdp = pdp(root, PolicyRepository.build("dag-staging").build(), targetIndex);
			for(int decision = 0; decision < 2; decision++){
				Result result = decide(pdp);
				assertEquals(Decision.PERMIT, result.getDecision());
				assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
				assertEquals(List.of("a", "b", "c"), orderedIds(result.getObligations()));
				assertEquals(List.of("a", "b", "c"), orderedIds(result.getAssociatedAdvice()));
			}
			return null;
		});
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void customDefinitionAfterSharedVariableDagKeepsResponsesWithItsDecision(boolean denied){
		Result result = withoutDebugLog(() -> {
			List<VariableDefinition> variables = sharedVariableDag(new VariableDefinition("level-0", BooleanVal.of(true)), 16);
			VariableDefinition writes = new VariableDefinition("writes", BooleanVal.of(true)){
				@Override public ValueExp evaluate(EvaluationContext context){
					addPermitResponses(context, "definition");
					return super.evaluate(context);
				}
			};
			Expression condition = and(new VariableReference(variables.getLast()), new VariableReference(writes));
			variables.add(writes);
			Policy custom = Policy.builder("dag-custom").version("1.0")
					.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
							URN + "1.0:rule-combining-algorithm:first-applicable"))
					.vars(variables).rule(Rule.builder("custom-rule", denied ? Effect.DENY : Effect.PERMIT)
							.condition(Condition.condition(condition))).build();
			PolicySet nested = PolicySet.builder("dag-custom-nested").version("1.0")
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
							URN + "1.0:policy-combining-algorithm:first-applicable"))
					.policy(custom).build();
			PolicySet root = PolicySet.builder("dag-custom-root").version("1.0")
					.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "3.0:policy-combining-algorithm:"
							+ (denied ? "permit-overrides" : "deny-overrides")))
					.policySet(nested).policy(orderedPolicy("winner")).build();
			return decide(pdp(root, PolicyRepository.build("dag-custom-staging").build()));
		});
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(denied ? List.of("winner") : List.of("definition-obligation", "winner"), orderedIds(result.getObligations()));
		assertEquals(denied ? List.of("winner") : List.of("definition-advice", "winner"), orderedIds(result.getAssociatedAdvice()));
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void customAlgorithmsKeepAllChildrenWhenTargetIndexIsEnabled(boolean policySet){
		Target otherResource = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("other"), resourceId(), FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow())
				.build()).build()).build();
		CompositeDecisionRule custom = policySet
				? PolicySet.builder("inventory-set").version("1.0").withCombiningAlgorithm(fullChildrenCombiner())
						.policy(Policy.builder("other-policy").version("1.0").target(otherResource)
								.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
								.rule(Rule.builder("other-rule", Effect.PERMIT)).build())
						.policy(silentPolicy("matching-policy", Effect.PERMIT)).build()
				: Policy.builder("inventory-policy").version("1.0").combiningAlgorithm(fullChildrenCombiner())
						.rule(Rule.builder("other-rule", Effect.PERMIT).target(otherResource))
						.rule(Rule.builder("matching-rule", Effect.PERMIT)).build();
		PolicySet root = PolicySet.builder("inventory-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.compositeDecisionRules(custom).build();
		PolicyDecisionPoint pdp = pdp(root, PolicyRepository.build("inventory-staging").build(), true);
		RequestContext request = RequestContext.builder().attributes(Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().attribute(Attribute.builder(RESOURCE_ID).value(StringVal.of("doc")).build()).build()).build()).build();
		for(int decision = 0; decision < 2; decision++){
			Result result = pdp.decide(request).getResults().iterator().next();
			assertEquals(Decision.PERMIT, result.getDecision());
			assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
			assertEquals(List.of("inventory-obligation"), orderedIds(result.getObligations()));
			assertEquals(List.of("inventory-advice"), orderedIds(result.getAssociatedAdvice()));
		}
	}

	/** A legitimate extension can decide from the full child inventory, including nonmatching targets. */
	private static <D extends DecisionRule> DecisionCombiningAlgorithm<D> fullChildrenCombiner(){
		return new DecisionCombiningAlgorithm<>() {
			@Override public String getId(){ return "inventory"; }
			@Override public Decision combine(EvaluationContext context, List<D> children){
				if(children.size() != 2){
					return Decision.DENY;
				}
				addPermitResponses(context, "inventory");
				return Decision.PERMIT;
			}
		};
	}

	/**
	 * Independent deterministic work probe, separate from the response outcomes above. The inline
	 * spy retains the exact engine class and the real getter; only its calls are counted. There is
	 * no elapsed-time assertion and no subclass that would trigger conservative fallback.
	 */
	@Test
	void responseCapabilityTraversalProbeReadsSharedDefinitionOncePerWalk() throws Exception{
		VariableDefinition leaf = spy(new VariableDefinition("level-0", BooleanVal.of(true)));
		assertEquals(VariableDefinition.class, leaf.getClass());
		Expression root = withoutDebugLog(() -> new VariableReference(sharedVariableDag(leaf, 12).getLast()));
		Method canWrite = Class.forName("org.xacml4j.v30.policy.ResponseWriters").getDeclaredMethod("canWrite", Expression.class);
		canWrite.setAccessible(true);
		// Building Apply arguments can inspect definitions too; count only the walk under test.
		long setupReads = expressionReads(leaf);
		for(int walk = 1; walk <= 2; walk++){
			assertFalse((boolean)canWrite.invoke(null, root));
			long reads = expressionReads(leaf) - setupReads;
			System.out.println("RESPONSE_CAPABILITY_PROBE depth=12 definitions=13 walk=" + walk + " cumulativeLeafReads=" + reads);
			assertEquals(walk, reads, "each walk must scan a shared exact definition once");
		}
	}

	private static long expressionReads(VariableDefinition definition){
		return mockingDetails(definition).getInvocations().stream()
				.filter(invocation -> invocation.getMethod().getName().equals("getExpression")).count();
	}

	private static List<VariableDefinition> sharedVariableDag(VariableDefinition leaf, int depth){
		List<VariableDefinition> variables = new ArrayList<>(List.of(leaf));
		for(int level = 1; level <= depth; level++){
			VariableDefinition below = variables.getLast();
			variables.add(new VariableDefinition("level-" + level, and(new VariableReference(below), new VariableReference(below))));
		}
		return variables;
	}

	private static Apply and(Expression first, Expression second){
		return Apply.builder(FUNCTIONS.getFunction(URN + "1.0:function:and").orElseThrow()).param(first, second).build();
	}

	/** A reference's debug text expands its whole DAG; suppress that unrelated work and restore logging. */
	private static <T> T withoutDebugLog(Supplier<T> work){
		Logger log = (Logger)LoggerFactory.getLogger("org.xacml4j");
		Level previous = log.getLevel();
		log.setLevel(Level.INFO);
		try{
			return work.get();
		}finally{
			log.setLevel(previous);
		}
	}

	/** A Deny rule with the given condition, beside a Permit policy, under permit-overrides */
	private static PolicySet denyBesideAPermit(Condition condition){
		Policy conditional = Policy.builder("conditional").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("conditional-rule", Effect.DENY).condition(condition))
				.build();
		return PolicySet.builder("conditional-root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:permit-overrides"))
				.policy(conditional).policy(silentPolicy("winner", Effect.PERMIT)).build();
	}

	private static void addPermitResponses(EvaluationContext context, String origin){
		context.addObligations(Decision.PERMIT, List.of(Obligation.builder(origin + "-obligation", Effect.PERMIT).build()));
		context.addAdvices(Decision.PERMIT, List.of(Advice.builder(origin + "-advice", Effect.PERMIT).build()));
	}

	private static AttributeDesignator resourceId(){
		return AttributeDesignator.builder().key(AttributeDesignatorKey.builder(RESOURCE_ID)
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build()).mustBePresent(false).build();
	}

	/** A target that is Indeterminate: it requires an attribute that the request does not have */
	private static Target missingAttributeTarget(){
		AttributeDesignator missing = AttributeDesignator.builder().key(AttributeDesignatorKey.builder("urn:xacml4j:test:missing")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build()).mustBePresent(true).build();
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("doc"), missing, FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow())
				.build()).build()).build();
	}

	/** A combining algorithm that adds Permit responses through its context, itself and from a worker it joins, and is Deny */
	private static <D extends DecisionRule> DecisionCombiningAlgorithm<D> customCombiner(){
		return new DecisionCombiningAlgorithm<>() {
			@Override public String getId(){ return "custom"; }
			@Override public Decision combine(EvaluationContext context, List<D> children){
				addPermitResponses(context, "custom");
				onJoinedWorker(() -> addPermitResponses(context, "worker"));
				return Decision.DENY;
			}
		};
	}

	/** Runs a task on a worker thread that the calling thread starts and joins */
	private static void onJoinedWorker(Runnable task){
		Thread worker = new Thread(task);
		worker.start();
		try{
			worker.join();
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	private static Policy orderedPolicy(String id){
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", Effect.PERMIT))
				.obligation(obligation(id, id))
				.advice(AdviceExpression.builder(id, Effect.PERMIT).attribute("origin", StringVal.of(id))).build();
	}

	private static Policy silentPolicy(String id, Effect effect){
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", effect)).build();
	}

	private static PolicyDecisionPoint pdp(PolicySet root, PolicyRepository repository){
		return pdp(root, repository, false);
	}

	private static PolicyDecisionPoint pdp(PolicySet root, PolicyRepository repository, boolean targetIndex){
		PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("staging-capabilities")
				.policyRepository(repository).pip(mock(PolicyInformationPoint.class)).rootPolicy(root);
		if(targetIndex){
			builder.targetIndex();
		}
		return builder.build();
	}

	private static Result decide(PolicyDecisionPoint pdp){
		return pdp.decide(RequestContext.builder().build()).getResults().iterator().next();
	}

	private static List<String> orderedIds(Collection<? extends DecisionRuleResponse> responses){
		return responses.stream().map(DecisionRuleResponse::getId).toList();
	}

	@Test
	void referencesBelowANestedSetAreResolvedFromTheRepositoryAndPublishOnce()
	{
		PolicySet referencedSet = PolicySet.builder("referenced-set").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(permit("inner"))
				.obligation(obligation("referenced-set-obligation", "referenced-set"))
				.build();
		PolicyRepository repository = PolicyRepository.build("nested-staging-repository").build();
		repository.add(permit("referenced"));
		repository.add(referencedSet);
		// deny-overrides evaluates every child, so both references are used.
		PolicySet nested = PolicySet.builder("nested").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.compositeDecisionRules(PolicyIDReference.builder("referenced").build(),
						PolicySetIDReference.builder("referenced-set").build())
				.obligation(obligation("nested-obligation", "nested"))
				.build();
		PolicySet root = PolicySet.builder("root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policySet(nested)
				.obligation(obligation("root-obligation", "root"))
				.build();
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("nested-staging")
				.policyRepository(repository)
				// The builder needs a PIP. The policies read no attribute.
				.pip(mock(PolicyInformationPoint.class))
				.rootPolicy(root)
				.build();

		Collection<Result> results = pdp.decide(RequestContext.builder().build()).getResults();

		assertEquals(1, results.size());
		Result result = results.iterator().next();
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("inner-obligation", "nested-obligation", "referenced-obligation",
				"referenced-set-obligation", "root-obligation"), ids(result.getObligations()));
		assertEquals(List.of("inner-advice", "referenced-advice"), ids(result.getAssociatedAdvice()));
		for (DecisionRuleResponse response : result.getObligations()) {
			assertOrigin(response);
		}
		for (DecisionRuleResponse response : result.getAssociatedAdvice()) {
			assertOrigin(response);
		}
	}

	/** The single "origin" assignment of a response names the policy or policy set that declared it. */
	private static void assertOrigin(DecisionRuleResponse response)
	{
		Collection<AttributeAssignment> origin = response.getAttribute("origin");
		assertEquals(1, origin.size(), response.getId());
		assertEquals(1, response.getAttributes().size(), response.getId());
		String declaredBy = response.getId().replaceFirst("-(obligation|advice)$", "");
		assertEquals(StringVal.of(declaredBy), origin.iterator().next().getAttribute(), response.getId());
	}

	/** A Permit policy with one obligation and one advice. */
	private static Policy permit(String id)
	{
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", Effect.PERMIT).build())
				.obligation(obligation(id + "-obligation", id))
				.advice(AdviceExpression.builder(id + "-advice", Effect.PERMIT)
						.attribute("origin", StringVal.of(id)))
				.build();
	}

	private static ObligationExpression.Builder obligation(String id, String origin)
	{
		return ObligationExpression.builder(id, Effect.PERMIT).attribute("origin", StringVal.of(origin));
	}

	/** The ids of the responses, sorted, with duplicates kept. */
	private static List<String> ids(Collection<? extends DecisionRuleResponse> responses)
	{
		List<String> ids = new ArrayList<>();
		for (DecisionRuleResponse response : responses) {
			ids.add(response.getId());
		}
		Collections.sort(ids);
		return ids;
	}
}
