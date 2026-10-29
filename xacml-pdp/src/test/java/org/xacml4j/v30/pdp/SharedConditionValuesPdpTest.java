package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithms;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionParametersValidator;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.AbstractPolicyRepository;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The rules of one policy that repeat an expensive condition subexpression share its value within a
 * decision (change optimize-pdp-evaluation, design D45). Every test reads decisions. Most compare them
 * with the same decisions observed: an observed decision evaluates every condition as it is written
 * (maintainer decision 2 of 2026-10-28).
 */
class SharedConditionValuesPdpTest
{
	private static final String FUNCTION = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String FIRST_APPLICABLE = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	private static final String POLICY_ALGORITHM = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:";
	/** The pattern of the rules, as in the repeated-conditions case of the rules corpus */
	private static final String PATTERN = "^[a-z0-9._]+@example\\.com$";
	private static final String EMAIL = "  Alice.Smith@Example.COM  ";
	private static final int RULES = 6;
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final Policies STANDARD = new Policies(FunctionProvider.builder().withDefaultFunctions().build());
	private static final AttributeDesignatorKey SUBJECT_EMAIL = key(CategoryId.SUBJECT_ACCESS, "urn:example:email");
	private static final AttributeDesignatorKey RESOURCE_EMAIL = key(CategoryId.RESOURCE, "urn:example:email");
	private static final AttributeDesignatorKey DEPARTMENT = key(CategoryId.SUBJECT_ACCESS, "urn:example:department");
	private static final AttributeDesignatorKey HOOK = key(CategoryId.RESOURCE, "urn:example:hook");
	private static final AttributeDesignatorKey OTHER = key(CategoryId.RESOURCE, "urn:example:other");

	static Stream<Arguments> requests(){
		return Stream.of(
				Arguments.of("department-5", List.of(EMAIL), Decision.PERMIT),
				Arguments.of("department-0", List.of(EMAIL), Decision.PERMIT),
				Arguments.of("department-9", List.of(EMAIL), Decision.NOT_APPLICABLE),
				Arguments.of("department-5", List.of("  Bob@Example.ORG "), Decision.NOT_APPLICABLE),
				Arguments.of("department-5", List.of("Alice Smith@example.com"), Decision.NOT_APPLICABLE),
				// A missing or multi-valued address fails in every rule; and is false where the department differs
				Arguments.of("department-5", List.of(), Decision.INDETERMINATE_P),
				Arguments.of("department-9", List.of(), Decision.NOT_APPLICABLE),
				Arguments.of("department-5", List.of(EMAIL, "bob@example.com"), Decision.INDETERMINATE_P),
				Arguments.of("department-9", List.of(EMAIL, "bob@example.com"), Decision.NOT_APPLICABLE),
				// A missing department fails after the address matched, in the first rule
				Arguments.of(null, List.of(EMAIL), Decision.INDETERMINATE_P));
	}

	@ParameterizedTest
	@MethodSource("requests")
	void rulesThatRepeatAnExpressionDecideAsWhenEachConditionIsEvaluated(String department, List<String> emails, Decision expected){
		Policy policy = STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
		RequestContext request = request(subject(department, emails));
		Result shared = decide(pdp(policy), request);
		assertEquals(expected, shared.getDecision());
		assertEquals(decide(observed(policy), request), shared);
	}

	@Test
	void aSharedValueBelongsToTheDecisionThatComputedIt(){
		PolicyDecisionPoint pdp = pdp(STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false))));
		assertEquals(Decision.PERMIT, decide(pdp, request(subject("department-5", List.of(EMAIL)))).getDecision());
		assertEquals(Decision.NOT_APPLICABLE, decide(pdp, request(subject("department-5", List.of("  Bob@Example.ORG ")))).getDecision());
		assertEquals(Decision.PERMIT, decide(pdp, request(subject("department-2", List.of(" carol@example.com")))).getDecision());
	}

	static Stream<Arguments> otherDesignators(){
		return Stream.of(
				// Another category: the resource's address, which the pattern does not match
				Arguments.of(designator(RESOURCE_EMAIL, false), Decision.NOT_APPLICABLE),
				// Another issuer: no attribute of the request has it, so the address is missing
				Arguments.of(designator(SUBJECT_EMAIL.withIssuer("urn:example:issuer"), false), Decision.INDETERMINATE_P),
				// MustBePresent: the same address
				Arguments.of(designator(SUBJECT_EMAIL, true), Decision.PERMIT));
	}

	/** The first four rules read the subject's address and the next four another designator; the department is the seventh rule's */
	@ParameterizedTest
	@MethodSource("otherDesignators")
	void anExpressionOverAnotherDesignatorHasAValueOfItsOwn(AttributeDesignator other, Decision expected){
		Policy policy = STANDARD.repeated(8, i -> STANDARD.normalized(i < 4 ? designator(SUBJECT_EMAIL, false) : other));
		RequestContext request = request(subject("department-6", List.of(EMAIL)), resource(RESOURCE_EMAIL, " Mallory@Elsewhere.ORG "));
		Result shared = decide(pdp(policy), request);
		assertEquals(expected, shared.getDecision());
		assertEquals(decide(observed(policy), request), shared);
	}

	/** A function with the id of the standard lower-case function that upper-cases: the pattern matches none of its four rules */
	@Test
	void aFunctionOfAnotherImplementationWithAStandardIdKeepsItsOwnValue(){
		FunctionSpec upper = function(FUNCTION + "string-normalize-to-lower-case",
				(context, value) -> StringVal.of(value.get().toUpperCase(Locale.ROOT)), null);
		Policy policy = STANDARD.repeated(8, i -> i < 4 ? STANDARD.normalized(designator(SUBJECT_EMAIL, false))
				: Apply.builder(upper).param(STANDARD.apply("string-normalize-space",
						STANDARD.apply("string-one-and-only", designator(SUBJECT_EMAIL, false)))).build());
		PolicyDecisionPoint pdp = pdp(policy);
		assertEquals(Decision.PERMIT, decide(pdp, request(subject("department-2", List.of(EMAIL)))).getDecision());
		assertEquals(Decision.NOT_APPLICABLE, decide(pdp, request(subject("department-6", List.of(EMAIL)))).getDecision());
	}

	/** A function that gives the id of the rule that calls it, with a validator of its own, over the shared address */
	@Test
	void aFunctionThatReadsItsContextIsEvaluatedInEachRule(){
		FunctionSpec ruleId = function("urn:example:function:rule-id",
				(context, value) -> StringVal.of(context.getCurrentRule().getId()), (spec, arguments) -> arguments.size() == 1);
		Policy.Builder policy = Policy.builder("rule-id").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE));
		for(int i = 0; i < RULES; i++){
			policy.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(new Condition(STANDARD.apply("string-equal",
					Apply.builder(ruleId).param(STANDARD.normalized(designator(SUBJECT_EMAIL, false))).build(),
					StringVal.of("rule-" + (RULES - 1))))));
		}
		assertEquals(Decision.PERMIT, decide(pdp(policy.build()), request(subject("department-0", List.of(EMAIL)))).getDecision());
	}

	/** A function of another implementation that takes its argument unevaluated gets it as written, not a shared stand-in */
	@Test
	void aFunctionOfAnotherImplementationGetsItsArgumentsAsWritten(){
		FunctionSpec written = FunctionSpecBuilder.builder("urn:example:function:written").param(XacmlTypes.STRING).lazyArgEval()
				.build(XacmlTypes.STRING, new FunctionInvocation(){
					@Override
					public ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
						return invoke(spec, context, Arrays.asList(arguments));
					}

					@Override
					public ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
						return StringVal.of(arguments.get(0) instanceof Apply ? "call" : "other");
					}
				});
		Policy.Builder policy = Policy.builder("written").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE));
		for(int i = 0; i < RULES; i++){
			policy.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(new Condition(STANDARD.apply("string-equal",
					Apply.builder(written).param(STANDARD.normalized(designator(SUBJECT_EMAIL, false))).build(),
					StringVal.of("call")))));
		}
		assertEquals(Decision.PERMIT, decide(pdp(policy.build()), request(subject("department-0", List.of(EMAIL)))).getDecision());
	}

	@ParameterizedTest
	@MethodSource("requests")
	void functionsOfAnotherInvocationFactoryDecideAsWhenEachConditionIsEvaluated(String department, List<String> emails, Decision expected){
		FunctionInvocationFactory delegating = new FunctionInvocationFactory(){
			@Override
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m){
				return FunctionInvocationFactory.defaultFactory().create(instance, m);
			}
		};
		Policies factory = new Policies(FunctionProviderBuilder.builder(delegating).build());
		Policy policy = factory.repeated(RULES, i -> factory.normalized(designator(SUBJECT_EMAIL, false)));
		RequestContext request = request(subject(department, emails));
		Result result = decide(pdp(policy), request);
		assertEquals(expected, result.getDecision());
		assertEquals(decide(observed(policy), request), result);
	}

	/**
	 * The address comes from the information point, which fails, then gives none, then gives it: the
	 * rules before the last read each answer, and the last one permits
	 */
	@Test
	void valuesOfTheInformationPointAreRetriedAndNotShared(){
		Policy policy = STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
		RequestContext request = request(subject("department-5", List.of()));
		Result shared = decide(pdp(policy, failingThenAbsentThenEmail(), null), request);
		assertEquals(Decision.PERMIT, shared.getDecision());
		assertEquals(decide(pdp(policy, failingThenAbsentThenEmail(), new Recorder()), request), shared);
	}

	@Test
	void anObservedDecisionRequestsTheAddressInEveryRule(){
		Policy policy = STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
		RequestContext request = request(subject("department-5", List.of(EMAIL)));
		Recorder recorder = new Recorder();
		Result observed = decide(pdp(policy, mock(PolicyInformationPoint.class), recorder), request);
		List<String> expected = new ArrayList<>();
		for(int i = 0; i < RULES; i++){
			expected.add("attribute " + SUBJECT_EMAIL.getAttributeId());
			expected.add("attribute " + DEPARTMENT.getAttributeId());
			expected.add("condition rule-" + i + " " + (i == RULES - 1 ? ConditionResult.TRUE : ConditionResult.FALSE));
		}
		assertEquals(expected, recorder.events);
		assertEquals(decide(pdp(policy), request), observed);
	}

	@Test
	void rulesEvaluatedOnAnotherThreadDecideAsOnTheDecidingThread(){
		ExecutorService worker = Executors.newSingleThreadExecutor();
		try{
			Policy elsewhere = STANDARD.repeated(new OnAWorker(worker), RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
			Policy here = STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
			for(Arguments arguments : requests().toList()){
				Object[] values = arguments.get();
				@SuppressWarnings("unchecked")
				RequestContext request = request(subject((String)values[0], (List<String>)values[1]));
				assertEquals(decide(pdp(here), request), decide(pdp(elsewhere), request), () -> Arrays.toString(values));
			}
		}finally{
			worker.shutdownNow();
		}
	}

	/** A literal string and a literal bag, the bag's values in order and with their count, are shared apart */
	@Test
	void literalsAreSharedByTheirTypedValues(){
		Expression single = XacmlTypes.STRING.bagOf(StringVal.of("  Dave@Example.COM "));
		Expression twice = XacmlTypes.STRING.bagOf(StringVal.of("  Dave@Example.COM "), StringVal.of("  Dave@Example.COM "));
		Policy bags = STANDARD.repeated(8, i -> STANDARD.normalized(i < 4 ? single : twice));
		Policy strings = STANDARD.repeated(8, i -> STANDARD.apply("string-normalize-to-lower-case",
				STANDARD.apply("string-normalize-space", StringVal.of(i < 4 ? "  Erin@Example.COM " : "  Erin@Elsewhere.ORG "))));
		RequestContext second = request(subject("department-2", List.of()));
		RequestContext seventh = request(subject("department-6", List.of()));
		assertEquals(Decision.PERMIT, decide(pdp(bags), second).getDecision());
		assertEquals(Decision.INDETERMINATE_P, decide(pdp(bags), seventh).getDecision());
		assertEquals(Decision.PERMIT, decide(pdp(strings), second).getDecision());
		assertEquals(Decision.NOT_APPLICABLE, decide(pdp(strings), seventh).getDecision());
		for(Policy policy : List.of(bags, strings)){
			for(RequestContext request : List.of(second, seventh)){
				assertEquals(decide(observed(policy), request), decide(pdp(policy), request));
			}
		}
	}

	/** The pattern above the shared address does not compile: the error stays with the rule that the department selects */
	@Test
	void aMalformedPatternAboveASharedValueKeepsItsDeferredError(){
		Policy.Builder policy = Policy.builder("malformed").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE));
		for(int i = 0; i < RULES; i++){
			policy.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(STANDARD.condition("^[a-z+@example\\.com$",
					STANDARD.normalized(designator(SUBJECT_EMAIL, false)), i)));
		}
		Policy built = policy.build();
		RequestContext request = request(subject("department-4", List.of(EMAIL)));
		Result shared = decide(pdp(built), request);
		assertEquals(Decision.INDETERMINATE_P, shared.getDecision());
		assertEquals(StatusCodeId.SYNTAX_ERROR, shared.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(RegExpFunctions.INVALID_PATTERN_MESSAGE), shared.getStatus().getMessage());
		assertEquals(decide(observed(built), request), shared);
	}

	static Stream<Arguments> fourRules(){
		return Stream.of(
				Arguments.of("department-2", List.of(EMAIL), Decision.PERMIT),
				Arguments.of("department-7", List.of(EMAIL), Decision.NOT_APPLICABLE),
				Arguments.of("department-2", List.of(), Decision.INDETERMINATE_P),
				Arguments.of(null, List.of(EMAIL), Decision.INDETERMINATE_P));
	}

	/**
	 * Four rules repeat the address in conditions of another class, which answer a false predicate but
	 * evaluate the one they were built with: the decisions and their statuses are those of that predicate,
	 * observed or not (review R-D45-sol)
	 */
	@ParameterizedTest
	@MethodSource("fourRules")
	void aConditionOfAnotherClassEvaluatesThePredicateItWasBuiltWith(String department, List<String> emails, Decision expected){
		Policy.Builder other = Policy.builder("repeated").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE));
		for(int i = 0; i < 4; i++){
			Condition condition = STANDARD.condition(PATTERN, STANDARD.normalized(designator(SUBJECT_EMAIL, false)), i);
			other.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(new AnswersFalse(condition.getExpression())));
		}
		Policy policy = other.build();
		RequestContext request = request(subject(department, emails));
		Result result = decide(pdp(policy), request);
		assertEquals(expected, result.getDecision());
		assertEquals(result, decide(observed(policy), request));
		Policy written = STANDARD.repeated(4, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
		assertEquals(decide(observed(written), request), result);
	}

	/** A repository event starts a new generation, which prepares the policy again */
	@Test
	void aNewGenerationDecidesAsTheOneBefore() throws Exception {
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("generations",
				FunctionProvider.builder().withDefaultFunctions().build(), ALGORITHMS);
		Field executor = AbstractPolicyRepository.class.getDeclaredField("notificationExecutor");
		executor.setAccessible(true);
		executor.set(repository, (java.util.concurrent.Executor)Runnable::run);
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("generations").policyRepository(repository)
				.rootPolicy(STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false))))
				.pip(mock(PolicyInformationPoint.class)).build();
		assertEquals(Decision.PERMIT, decide(pdp, request(subject("department-3", List.of(EMAIL)))).getDecision());
		repository.add(Policy.builder("unrelated").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE))
				.rule(Rule.builder("unrelated-rule", Effect.DENY).build()).build());
		assertEquals(Decision.NOT_APPLICABLE, decide(pdp, request(subject("department-3", List.of("  Bob@Example.ORG ")))).getDecision());
		assertEquals(Decision.PERMIT, decide(pdp, request(subject("department-1", List.of(EMAIL)))).getDecision());
	}

	/**
	 * A decision that shared values misses a reference, while a nested decision pins it: the index of a
	 * later set must stay off, so the miss is replayed there and the decision is Indeterminate. With the
	 * index on, the pinned position, whose target wants another value, would be skipped, and the set would
	 * permit (review PR5 P1-1 of D24/D25).
	 */
	@Test
	void aReferenceMissAfterSharedValuesIsStillReplayed(){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicy("shared", null, null, null)).thenReturn(null, otherPolicy("shared", "wanted"));
		PolicyIDReference ref = PolicyIDReference.builder("shared").build();
		PolicySet first = set("first", "permit-unless-deny", List.of(ref));
		PolicySet second = set("second", "deny-overrides", List.of(ref, otherPolicy("different", "different")));
		Policy repeated = STANDARD.repeated(RULES, i -> STANDARD.normalized(designator(SUBJECT_EMAIL, false)));
		PolicySet root = set("root", "deny-overrides", List.of(repeated, first, hookPolicy(), second));
		RequestContext request = request(subject("department-9", List.of(EMAIL)), resource(OTHER, "different"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		AtomicReference<PolicyDecisionPoint> pdp = new AtomicReference<>();
		AtomicBoolean nested = new AtomicBoolean();
		when(pip.resolve(any(EvaluationContext.class), eq(HOOK))).thenAnswer(call -> {
			if(nested.compareAndSet(false, true)) assertEquals(Decision.PERMIT, decide(pdp.get(), request).getDecision());
			return Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("go")));
		});
		pdp.set(PolicyDecisionPointBuilder.builder("misses").policyRepository(repository).rootPolicy(root).pip(pip)
				.targetIndex().build());
		assertEquals(Decision.INDETERMINATE_DP, decide(pdp.get(), request).getDecision());
	}

	private static PolicyInformationPoint failingThenAbsentThenEmail(){
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(EvaluationContext.class), eq(SUBJECT_EMAIL)))
				.thenThrow(new IllegalStateException("unavailable"))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(XacmlTypes.STRING.bagOf(StringVal.of(EMAIL))));
		return pip;
	}

	/** A policy that permits when the request's resource has the other attribute with the given value */
	private static Policy otherPolicy(String id, String value){
		AttributeDesignator other = designator(OTHER, false);
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of(value), other, STANDARD.function("string-equal")).build()).build()).build();
		return Policy.builder(id).version("1.0").target(target).combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE))
				.rule(Rule.builder(id + "-rule", Effect.PERMIT).build()).build();
	}

	/** A policy whose condition asks the information point for the hook */
	private static Policy hookPolicy(){
		return Policy.builder("hook-policy").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE))
				.rule(Rule.builder("hook-rule", Effect.PERMIT).condition(new Condition(STANDARD.apply("string-is-in",
						StringVal.of("go"), designator(HOOK, true))))).build();
	}

	private static PolicySet set(String id, String algorithm, List<CompositeDecisionRule> children){
		return PolicySet.builder(id).version("1.0").withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(POLICY_ALGORITHM + algorithm))
				.compositeDecisionRules(children).build();
	}

	/** A function of one string argument and a string value, built with the given validator, or none */
	private static FunctionSpec function(String id, BiFunction<EvaluationContext, StringVal, StringVal> body,
			FunctionParametersValidator validator){
		return FunctionSpecBuilder.builder(id).param(XacmlTypes.STRING).build(XacmlTypes.STRING, validator, new FunctionInvocation(){
			@Override
			public ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
				return invoke(spec, context, Arrays.asList(arguments));
			}

			@Override
			public ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
				return body.apply(context, (StringVal)arguments.get(0));
			}
		});
	}

	private static PolicyDecisionPoint pdp(CompositeDecisionRule root){
		return pdp(root, mock(PolicyInformationPoint.class), null);
	}

	/** A decision point whose decisions an observer observes, so that they evaluate every condition as it is written */
	private static PolicyDecisionPoint observed(CompositeDecisionRule root){
		return pdp(root, mock(PolicyInformationPoint.class), new Recorder());
	}

	private static PolicyDecisionPoint pdp(CompositeDecisionRule root, PolicyInformationPoint pip, DecisionObserver<?> observer){
		PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("shared-conditions")
				.policyRepository(mock(PolicyRepository.class)).rootPolicy(root).pip(pip);
		if(observer != null) builder.observer(observer);
		return builder.build();
	}

	private static Result decide(PolicyDecisionPoint pdp, RequestContext request){
		return pdp.decide(request).getResults().iterator().next();
	}

	private static RequestContext request(Category... categories){
		return RequestContext.builder().attributes(categories).build();
	}

	/** The subject's department, unless null, and its addresses, if any */
	private static Category subject(String department, List<String> emails){
		Entity.Builder subject = Entity.builder();
		if(department != null){
			subject.attribute(Attribute.builder(DEPARTMENT.getAttributeId()).value(StringVal.of(department)).build());
		}
		if(!emails.isEmpty()){
			subject.attribute(Attribute.builder(SUBJECT_EMAIL.getAttributeId())
					.value(emails.stream().map(StringVal::of).toArray(StringVal[]::new)).build());
		}
		return Category.subjectAccess().entity(subject.build()).build();
	}

	private static Category resource(AttributeDesignatorKey key, String value){
		return Category.resource().entity(Entity.builder()
				.attribute(Attribute.builder(key.getAttributeId()).value(StringVal.of(value)).build()).build()).build();
	}

	private static AttributeDesignatorKey key(CategoryId category, String attributeId){
		return AttributeDesignatorKey.builder(attributeId).category(category).dataType(XacmlTypes.STRING).build();
	}

	private static AttributeDesignator designator(AttributeDesignatorKey key, boolean mustBePresent){
		return AttributeDesignator.builder().key(key).mustBePresent(mustBePresent).build();
	}

	/** The policies of the tests, over one set of functions */
	private record Policies(FunctionProvider functions)
	{
		/** Rules that permit, the first applicable deciding: rule i for department-i and an address that the pattern matches */
		Policy repeated(int rules, IntFunction<Expression> email){
			return repeated(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE), rules, email);
		}

		Policy repeated(DecisionCombiningAlgorithm<Rule> algorithm, int rules, IntFunction<Expression> email){
			Policy.Builder policy = Policy.builder("repeated").version("1.0").combiningAlgorithm(algorithm);
			for(int i = 0; i < rules; i++){
				policy.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(condition(PATTERN, email.apply(i), i)));
			}
			return policy.build();
		}

		/** and(string-regexp-match(pattern, email), string-equal(string-one-and-only(department), "department-i")) */
		Condition condition(String pattern, Expression email, int rule){
			return new Condition(apply("and",
					apply("string-regexp-match", StringVal.of(pattern), email),
					apply("string-equal", apply("string-one-and-only", designator(DEPARTMENT, false)), StringVal.of("department-" + rule))));
		}

		/** The one string of a bag, without its outer spaces and in lower case */
		Expression normalized(Expression bag){
			return apply("string-normalize-to-lower-case", apply("string-normalize-space", apply("string-one-and-only", bag)));
		}

		Apply apply(String function, Expression... arguments){
			return Apply.builder(function(function)).param(arguments).build();
		}

		FunctionSpec function(String name){
			return functions.getFunction(FUNCTION + name).orElseThrow();
		}
	}

	/** A condition that answers a false predicate, but evaluates the one it was built with */
	private static final class AnswersFalse extends Condition
	{
		AnswersFalse(Expression predicate){
			super(predicate);
		}

		@Override
		public Expression getExpression(){
			return BooleanVal.of(false);
		}
	}

	/** Records the attributes that are requested and the conditions that are evaluated, in order */
	private static final class Recorder implements DecisionObserver<String>
	{
		private final List<String> events = new ArrayList<>();

		@Override
		public String requestStarted(PolicyDecisionPoint pdp, RequestContext request){
			return "request";
		}

		@Override
		public String attributeRequested(String parent, EvaluationContext context, AttributeReferenceKey key){
			events.add("attribute " + ((AttributeDesignatorKey)key).getAttributeId());
			return parent;
		}

		@Override
		public void conditionEvaluated(String current, EvaluationContext context, DecisionRule rule, ConditionResult result){
			events.add("condition " + rule.getId() + " " + result);
		}
	}

	/** First-applicable, with each rule evaluated on the thread of an executor */
	private record OnAWorker(ExecutorService worker) implements DecisionCombiningAlgorithm<Rule>
	{
		@Override
		public String getId(){
			return "urn:example:rule-combining-algorithm:on-a-worker";
		}

		@Override
		public Decision combine(EvaluationContext context, List<Rule> rules){
			for(Rule rule : rules){
				Future<Decision> evaluation = worker.submit(() -> DecisionCombiningAlgorithms.evaluate(context, rule));
				Decision decision;
				try{
					decision = evaluation.get();
				}catch(InterruptedException e){
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e);
				}catch(ExecutionException e){
					throw new IllegalStateException(e.getCause());
				}
				if(decision != Decision.NOT_APPLICABLE){
					return decision;
				}
			}
			return Decision.NOT_APPLICABLE;
		}
	}
}
