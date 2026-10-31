package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * A rule that a built-in combining algorithm evaluates may be evaluated in the context of its policy
 * instead of a context of its own (change optimize-pdp-evaluation, design D47). Every test decides a
 * policy in a root that observes the decision, whose rules keep contexts of their own, and in one that
 * does not, and compares the decision, its status, the status by each rule and by the policy, and the
 * responses.
 */
class RulesInPolicyContextTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	/** The one attribute of the request: the string "yes" */
	private static final AttributeDesignatorKey PRESENT = key("present");
	private static final AttributeDesignatorKey ABSENT = key("absent");
	private static final AttributeDesignatorKey ABSENT_FROM_POLICY = key("absent-from-policy");
	/** A variable that no policy defines: a rule that reads it fails without a status of its own */
	private static final VariableDefinition UNDEFINED = new VariableDefinition("undefined", BooleanVal.of(true));

	/** The status of the second failing rule is its own fallback processing-error, also by the rule and by the policy */
	@Test
	void aRuleThatFailsAfterAnotherReportsItsOwnStatus(){
		Policy policy = policy("3.0:rule-combining-algorithm:deny-overrides")
				.rule(Rule.builder("missing", Effect.PERMIT).condition(equalsYes(ABSENT)))
				.rule(Rule.builder("undefined", Effect.PERMIT).condition(new Condition(new VariableReference(UNDEFINED))))
				.build();

		Decided decided = decide(policy, false);

		assertEquals(Decision.INDETERMINATE_P, decided.decision());
		assertEquals(Status.processingError().build(), decided.status().orElseThrow());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, decided.byRule().get(0).orElseThrow().getStatusCode().getValue());
		assertEquals(Status.processingError().build(), decided.byRule().get(1).orElseThrow());
		assertEquals(Status.processingError().build(), decided.byPolicy().orElseThrow());
		assertEquals(decide(policy, true), decided);
	}

	/** XACML 3.0, section 7.11: the pass of a policy whose target failed keeps the status of that failure */
	@Test
	void aRuleThatFailsInThePassOfItsPolicyKeepsTheStatusThatStartedIt(){
		Policy policy = policy("3.0:rule-combining-algorithm:deny-overrides")
				.target(Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder().match(
						StringVal.of("yes"), designator(ABSENT_FROM_POLICY), function("string-equal")).build()).build()))
				.rule(Rule.builder("missing", Effect.PERMIT).condition(equalsYes(ABSENT)))
				.build();

		Decided decided = decide(policy, false);

		assertEquals(Decision.INDETERMINATE_P, decided.decision());
		assertTrue(decided.status().orElseThrow().getMessage().orElseThrow().contains(ABSENT_FROM_POLICY.getAttributeId()));
		assertEquals(decided.status(), decided.byPolicy());
		assertTrue(decided.byRule().get(0).orElseThrow().getMessage().orElseThrow().contains(ABSENT.getAttributeId()));
		assertEquals(decide(policy, true), decided);
	}

	static Stream<Arguments> algorithms(){
		return Stream.of("1.0:rule-combining-algorithm:first-applicable",
						"3.0:rule-combining-algorithm:deny-overrides", "3.0:rule-combining-algorithm:ordered-deny-overrides",
						"3.0:rule-combining-algorithm:permit-overrides", "3.0:rule-combining-algorithm:ordered-permit-overrides",
						"3.0:rule-combining-algorithm:deny-unless-permit", "3.0:rule-combining-algorithm:permit-unless-deny",
						"1.0:rule-combining-algorithm:deny-overrides", "1.1:rule-combining-algorithm:ordered-deny-overrides",
						"1.0:rule-combining-algorithm:permit-overrides", "1.1:rule-combining-algorithm:ordered-permit-overrides")
				.flatMap(algorithm -> IntStream.range(0, 5).mapToObj(rotation -> Arguments.of(algorithm, rotation)));
	}

	/** Permit and Deny with responses, Indeterminate of either effect, and NotApplicable, starting with each */
	@ParameterizedTest(name = "{0}, rotated by {1}")
	@MethodSource("algorithms")
	void rulesOfMixedEffectsDecideAsInContextsOfTheirOwn(String algorithm, int rotation){
		List<Rule> rules = new ArrayList<>(List.of(
				Rule.builder("permit", Effect.PERMIT).obligation(obligation("permit", Effect.PERMIT, StringVal.of("permit")))
						.advice(AdviceExpression.builder("permit", Effect.PERMIT).attribute("origin", StringVal.of("permit"))).build(),
				Rule.builder("deny", Effect.DENY).obligation(obligation("deny", Effect.DENY, StringVal.of("deny"))).build(),
				Rule.builder("missing", Effect.PERMIT).condition(equalsYes(ABSENT)).build(),
				Rule.builder("undefined", Effect.DENY).condition(new Condition(new VariableReference(UNDEFINED))).build(),
				Rule.builder("false", Effect.PERMIT).condition(new Condition(
						apply("string-equal", oneAndOnly(PRESENT), StringVal.of("no")))).build()));
		Collections.rotate(rules, rotation);
		Policy policy = policy(algorithm).rules(rules).build();

		assertEquals(decide(policy, true), decide(policy, false));
	}

	/** A variable that one rule evaluates is the value of the next; the definitions form a diamond */
	@Test
	void theVariablesOfAPolicyAreSharedByItsRules(){
		VariableDefinition answer = new VariableDefinition("answer", oneAndOnly(PRESENT));
		VariableDefinition yes = new VariableDefinition("yes", apply("string-equal", new VariableReference(answer), StringVal.of("yes")));
		VariableDefinition no = new VariableDefinition("no", apply("string-equal", new VariableReference(answer), StringVal.of("no")));
		VariableDefinition either = new VariableDefinition("either", apply("or", new VariableReference(yes), new VariableReference(no)));
		Policy policy = policy("1.0:rule-combining-algorithm:first-applicable").var(answer).var(yes).var(no).var(either)
				.rule(Rule.builder("no", Effect.DENY).condition(new Condition(new VariableReference(no))))
				.rule(Rule.builder("both", Effect.PERMIT)
						.condition(new Condition(apply("and", new VariableReference(either), new VariableReference(yes))))
						.obligation(obligation("answer", Effect.PERMIT, new VariableReference(answer))))
				.build();

		Decided decided = decide(policy, false);

		assertEquals(Decision.PERMIT, decided.decision());
		assertEquals(List.of("answer"), decided.obligations().stream().map(Obligation::getId).toList());
		assertEquals(List.of(StringVal.of("yes")), decided.obligations().get(0).getAttributes().stream()
				.map(AttributeAssignment::getAttribute).toList());
		assertEquals(decide(policy, true), decided);
	}

	/** A decision evaluated on another thread than the one that created its root decides alike */
	@Test
	void aPolicyEvaluatedOnAnotherThreadDecidesAlike() throws Exception{
		Policy policy = policy("3.0:rule-combining-algorithm:deny-overrides")
				.rule(Rule.builder("missing", Effect.PERMIT).condition(equalsYes(ABSENT)))
				.rule(Rule.builder("undefined", Effect.PERMIT).condition(new Condition(new VariableReference(UNDEFINED))))
				.rule(Rule.builder("permit", Effect.PERMIT).obligation(obligation("permit", Effect.PERMIT, StringVal.of("permit"))))
				.build();
		RootEvaluationContext root = root(false);
		ExecutorService worker = Executors.newSingleThreadExecutor();
		try{
			Decision decision = worker.submit(() -> policy.evaluate(policy.createContext(root))).get();
			assertEquals(decide(policy, false), decided(root, policy, decision));
		}finally{
			worker.shutdownNow();
		}
	}

	/** A decision of a policy and what the root of the decision holds after it */
	private record Decided(Decision decision, Optional<Status> status, List<Optional<Status>> byRule,
			Optional<Status> byPolicy, List<Obligation> obligations, List<Advice> advice) { }

	private static Decided decide(Policy policy, boolean observed){
		RootEvaluationContext root = root(observed);
		return decided(root, policy, policy.evaluate(policy.createContext(root)));
	}

	private static Decided decided(RootEvaluationContext root, Policy policy, Decision decision){
		List<Optional<Status>> byRule = new ArrayList<>();
		for(Rule rule : policy.getRules()){
			byRule.add(root.getEvaluationStatus(rule));
		}
		return new Decided(decision, root.getEvaluationStatus(), byRule, root.getEvaluationStatus(policy),
				List.copyOf(root.getMatchingObligations(decision)), List.copyOf(root.getMatchingAdvices(decision)));
	}

	/** The root of a decision of the request, which an observer observes or not */
	private static RootEvaluationContext root(boolean observed){
		return new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO, XPathVersion.XPATH1,
				mock(PolicyReferenceResolver.class), new Request(), false,
				observed ? DecisionObservation.start((pdp, request) -> "request", "request", RequestContext.builder().build(), 1, 1)
						: DecisionObservation.none());
	}

	private static Policy.Builder policy(String algorithm){
		return Policy.builder("policy").version("1.0").combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + algorithm));
	}

	/** string-equal(string-one-and-only(designator), "yes"), with the designator MustBePresent */
	private static Condition equalsYes(AttributeDesignatorKey key){
		return new Condition(apply("string-equal", apply("string-one-and-only", designator(key)), StringVal.of("yes")));
	}

	private static Expression oneAndOnly(AttributeDesignatorKey key){
		return apply("string-one-and-only", designator(key));
	}

	private static ObligationExpression.Builder obligation(String id, Effect effect, Expression origin){
		return ObligationExpression.builder(id, effect).attribute("origin", origin);
	}

	private static AttributeDesignator designator(AttributeDesignatorKey key){
		return AttributeDesignator.builder().key(key).mustBePresent(true).build();
	}

	private static Apply apply(String function, Expression... arguments){
		return Apply.builder(function(function)).param(arguments).build();
	}

	private static FunctionSpec function(String name){
		return FUNCTIONS.getFunction(URN + "1.0:function:" + name).orElseThrow();
	}

	private static AttributeDesignatorKey key(String id){
		return AttributeDesignatorKey.builder("urn:example:" + id).category(CategoryId.SUBJECT_ACCESS)
				.dataType(XacmlTypes.STRING).build();
	}

	/** A request whose one attribute is {@link #PRESENT} */
	private static final class Request implements EvaluationContextHandler
	{
		@Override
		public Optional<BagOfValues> resolve(EvaluationContext context, AttributeReferenceKey key){
			return key.equals(PRESENT) ? Optional.of(StringVal.of("yes").toBag()) : Optional.empty();
		}

		@Override
		public Optional<Entity> getEntity(CategoryId id){
			return Optional.empty();
		}

		@Override
		public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators(){
			return Map.of();
		}

		@Override
		public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors(){
			return Map.of();
		}
	}
}
