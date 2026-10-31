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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.AdditionalAnswers;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.IndexCustomChild;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicyReferenceResolver;
import org.xacml4j.v30.policy.PreparedPolicySets;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.combine.impl.OnPermitApplySecondPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pdp.NoCachePolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.AbstractPolicyRepository;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

class TargetIndexPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionSpec EQUAL = FunctionProvider.builder().withDefaultFunctions().build()
			.getFunction(URN + "1.0:function:string-equal").orElseThrow();
	private static final AttributeDesignatorKey KEY = key("resource");
	/** An observer that records nothing; an observed decision matches every target in its own context (design D41) */
	private static final DecisionObserver<Object> OBSERVED = (pdp, request) -> request;

	static Stream<Arguments> requests(){
		return Stream.of(Arguments.of(List.of("alpha"), Decision.PERMIT),
				Arguments.of(List.of("beta"), Decision.PERMIT), Arguments.of(List.of("gamma"), Decision.DENY),
				Arguments.of(List.of("alpha", "gamma", "alpha"), Decision.DENY),
				Arguments.of(List.of("other"), Decision.NOT_APPLICABLE), Arguments.of(List.of(), Decision.PERMIT))
				.flatMap(item -> Stream.of(false, true).map(validate -> {
					Object[] values = item.get();
					return Arguments.of(values[0], values[1], validate);
				}));
	}

	static Stream<Arguments> builtInTargets(){
		return Stream.of(
				Arguments.of("integer-equal", IntegerVal.of(5), IntegerVal.of(6), Decision.PERMIT),
				Arguments.of("boolean-equal", BooleanVal.of(true), BooleanVal.of(false), Decision.PERMIT),
				Arguments.of("anyURI-equal", XacmlTypes.ANYURI.ofAny("urn:test:a"),
						XacmlTypes.ANYURI.ofAny("urn:test:b"), Decision.PERMIT),
				Arguments.of("double-equal", DoubleVal.of(0.0), DoubleVal.of(-0.0), Decision.DENY),
				Arguments.of("double-equal", DoubleVal.of(Double.NaN), DoubleVal.of(Double.NaN), Decision.PERMIT));
	}

	@ParameterizedTest
	@MethodSource("builtInTargets")
	void builtInTargetDecisionsKeepTheirFunctionSemantics(String function, Value<?> literal, Value<?> actual,
			Decision expected) throws Exception {
		AttributeDesignatorKey ref = AttributeDesignatorKey.builder("primitive").category(CategoryId.RESOURCE)
				.dataType(literal.getEvaluatesTo().getValueType()).build();
		FunctionSpec predicate = FunctionProvider.builder().withDefaultFunctions().build()
				.getFunction(URN + "1.0:function:" + function).orElseThrow();
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(literal, designator(ref, false), predicate).build()).build()).build();
		PolicySet root = set("primitives", "1.0:first-applicable", List.of(
				policy("test", Effect.DENY, target), policy("fallback", Effect.PERMIT, null)));
		RequestContext request = RequestContext.builder().attributes(Category.resource().entity(Entity.builder()
				.attribute(Attribute.builder("primitive").value(actual).build()).build()).build()).build();
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(expected, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "observed", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@ParameterizedTest
	@MethodSource("requests")
	void policyTargetsAndMultipleRequestValuesMatchTheLinearResponse(List<String> values, Decision expected, boolean validate)
			throws Exception {
		PolicySet root = set("root", "3.0:deny-overrides", List.of(
				policy("permit", Effect.PERMIT, target(KEY, false, EQUAL, "alpha", "beta")),
				policy("deny", Effect.DENY, target(KEY, false, EQUAL, "gamma"))));
		PolicyInformationPoint pip = pip(KEY, "alpha");
		RequestContext request = request(values);
		Result baseline = decide(root, repository(), pip, request, "baseline", validate);
		assertEquals(expected, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", validate));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", validate));
	}

	@ParameterizedTest
	@MethodSource("requests")
	void ruleTargetsMatchTheLinearResponse(List<String> values, Decision expected, boolean validate) throws Exception {
		Policy root = Policy.builder("rules").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:deny-overrides"))
				.rule(Rule.builder("permit", Effect.PERMIT).target(target(KEY, false, EQUAL, "alpha", "beta")))
				.rule(Rule.builder("deny", Effect.DENY).target(target(KEY, false, EQUAL, "gamma"))).build();
		PolicyInformationPoint pip = pip(KEY, "alpha");
		RequestContext request = request(values);
		Result baseline = decide(root, repository(), pip, request, "baseline", validate);
		assertEquals(expected, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", validate));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", validate));
	}

	@ParameterizedTest
	@ValueSource(ints = {2, 9})
	void repeatedAndMultiValueRequestsKeepResponseOrder(int alwaysCount) throws Exception {
		List<CompositeDecisionRule> children = new ArrayList<>();
		children.add(policy("alpha-first", Effect.PERMIT, target(KEY, false, EQUAL, "alpha", "alpha")));
		for(int i = 0; i < alwaysCount; i++) children.add(policy("always-" + i, Effect.PERMIT, null));
		children.add(policy("beta", Effect.PERMIT, target(KEY, false, EQUAL, "beta")));
		children.add(policy("alpha-last", Effect.PERMIT, target(KEY, false, EQUAL, "alpha")));
		PolicySet root = set("candidate-order", "3.0:deny-overrides", children);
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		PolicyDecisionPoint indexed = PolicyDecisionPointBuilder.builder("candidate-order")
				.rootPolicy(root).policyRepository(repository()).pip(pip).targetIndex().build();

		for(List<String> values : List.of(List.of("alpha"), List.of("beta"), List.of("other"),
				List.of("beta", "alpha", "alpha"), List.<String>of(), List.of("alpha"))){
			RequestContext request = request(values);
			Result expected = decide(root, repository(), pip, request, "baseline", false);
			Result actual = indexed.decide(request).getResults().iterator().next();
			assertEquals(Decision.PERMIT, actual.getDecision());
			assertResponse(expected, actual);
			List<String> obligations = new ArrayList<>();
			if(values.contains("alpha")) obligations.add("alpha-first-obligation");
			for(int i = 0; i < alwaysCount; i++) obligations.add("always-" + i + "-obligation");
			if(values.contains("beta")) obligations.add("beta-obligation");
			if(values.contains("alpha")) obligations.add("alpha-last-obligation");
			assertEquals(Set.copyOf(obligations), Set.copyOf(actual.getObligations().stream()
					.map(DecisionRuleResponse::getId).toList()));
		}
	}

	@Test
	void firstApplicableKeepsIndexedAndAlwaysCandidateOrder() throws Exception {
		PolicySet root = set("first-candidate", "1.0:first-applicable", List.of(
				policy("alpha", Effect.PERMIT, target(KEY, false, EQUAL, "alpha")),
				policy("always", Effect.DENY, null),
				policy("beta", Effect.PERMIT, target(KEY, false, EQUAL, "beta"))));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		PolicyDecisionPoint indexed = PolicyDecisionPointBuilder.builder("first-candidate")
				.rootPolicy(root).policyRepository(repository()).pip(pip).targetIndex().build();
		for(String value : List.of("alpha", "beta", "other", "alpha")){
			RequestContext request = request(List.of(value));
			Result actual = indexed.decide(request).getResults().iterator().next();
			assertEquals(value.equals("alpha") ? Decision.PERMIT : Decision.DENY, actual.getDecision());
			assertResponse(decide(root, repository(), pip, request, "baseline", false), actual);
		}
	}

	@Test
	void singlePolicyAndRuleKeepDecisionsAndResponses() throws Exception {
		for(boolean targeted : List.of(false, true)){
			Policy.Builder builder = Policy.builder("single-policy").version("1.0")
					.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
					.rule(Rule.builder("single-rule", Effect.PERMIT).target(target(KEY, false, EQUAL, "alpha"))
							.obligation(ObligationExpression.builder("rule-obligation", Effect.PERMIT)))
					.obligation(ObligationExpression.builder("policy-obligation", Effect.PERMIT));
			if(targeted) builder.target(target(KEY, false, EQUAL, "alpha"));
			PolicySet root = set("single-set", "1.0:first-applicable", List.of(builder.build()));
			PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
			for(String value : List.of("alpha", "other")){
				RequestContext request = request(List.of(value));
				Result expected = decide(root, repository(), pip, request, "off", false);
				Result actual = decide(root, repository(), pip, request, "on", false);
				assertEquals(value.equals("alpha") ? Decision.PERMIT : Decision.NOT_APPLICABLE, actual.getDecision());
				assertResponse(expected, actual);
				if(value.equals("alpha")){
					assertEquals(Set.of("rule-obligation", "policy-obligation"), Set.copyOf(actual.getObligations().stream()
							.map(DecisionRuleResponse::getId).toList()));
				}
			}
		}
	}

	@Test
	void nonIndexableRangeRulesKeepFirstApplicableResponses() throws Exception {
		AttributeDesignatorKey size = AttributeDesignatorKey.builder("size").category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.INTEGER).build();
		FunctionSpec less = FunctionProvider.builder().withDefaultFunctions().build()
				.getFunction(URN + "1.0:function:integer-less-than").orElseThrow();
		Target twenty = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(IntegerVal.of(20), designator(size, false), less).build()).build()).build();
		Target ten = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(IntegerVal.of(10), designator(size, false), less).build()).build()).build();
		Policy policy = Policy.builder("ranges").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("first", Effect.DENY).target(twenty))
				.rule(Rule.builder("second", Effect.PERMIT).target(ten)
						.obligation(ObligationExpression.builder("permit-obligation", Effect.PERMIT)))
				.rule(Rule.builder("fallback", Effect.DENY)
						.obligation(ObligationExpression.builder("deny-obligation", Effect.DENY))).build();
		PolicySet root = set("range-set", "1.0:first-applicable", List.of(policy));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		for(long value : List.of(12L, 3L)){
			RequestContext request = RequestContext.builder().returnPolicyIdList(true).attributes(Category.resource()
					.entity(Entity.builder().attribute(Attribute.builder("size").value(IntegerVal.of(value)).build()).build())
					.build()).build();
			Result expected = decide(root, repository(), pip, request, "off", false);
			Result actual = decide(root, repository(), pip, request, "on", false);
			assertEquals(value == 12 ? Decision.PERMIT : Decision.DENY, actual.getDecision());
			assertResponse(expected, actual);
			assertEquals(List.of(value == 12 ? "permit-obligation" : "deny-obligation"),
					actual.getObligations().stream().map(DecisionRuleResponse::getId).toList());
		}
	}

	static Stream<Arguments> integerRangeBags(){
		return Stream.of(
				Arguments.of(List.of(9L), Decision.PERMIT),
				Arguments.of(List.of(10L), Decision.DENY),
				Arguments.of(List.of(19L), Decision.DENY),
				Arguments.of(List.of(20L), Decision.PERMIT),
				Arguments.of(List.of(5L, 25L), Decision.DENY),
				Arguments.of(List.of(10L, 10L), Decision.DENY),
				Arguments.of(List.of(Long.MIN_VALUE, Long.MAX_VALUE), Decision.DENY));
	}

	@ParameterizedTest
	@MethodSource("integerRangeBags")
	void integerBoundsUseSeparateWitnessesAndKeepWholeResponses(List<Long> values, Decision expected) throws Exception {
		AttributeDesignatorKey size = rangeKey("size", XacmlTypes.INTEGER);
		Policy policy = rangePolicy("ranges", "1.0", range(size, IntegerVal.of(10), IntegerVal.of(20), false));
		RequestContext request = rangeRequest("size", values.stream().map(IntegerVal::of).toList());
		assertRangeModes(policy, repository(), mock(PolicyInformationPoint.class), request, expected);
	}

	@Test
	void rangeAlternativesOverlapsReversedBoundsAndOrderKeepWholeResponses() throws Exception {
		AttributeDesignatorKey size = rangeKey("size", XacmlTypes.INTEGER);
		MatchAnyOf alternatives = MatchAnyOf.builder()
				.anyOf(bounds(size, IntegerVal.of(10), IntegerVal.of(20), false))
				.anyOf(bounds(size, IntegerVal.of(30), IntegerVal.of(40), false)).build();
		Policy policy = Policy.builder("alternatives").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:deny-overrides"))
				.rule(Rule.builder("first", Effect.PERMIT).target(Target.builder().anyOf(alternatives).build())
						.obligation(ObligationExpression.builder("first", Effect.PERMIT)))
				.rule(Rule.builder("overlap", Effect.PERMIT).target(range(size, IntegerVal.of(15), IntegerVal.of(35), false))
						.obligation(ObligationExpression.builder("overlap", Effect.PERMIT)))
				.rule(Rule.builder("duplicate", Effect.PERMIT).target(range(size, IntegerVal.of(15), IntegerVal.of(35), false))
						.obligation(ObligationExpression.builder("duplicate", Effect.PERMIT)))
				.rule(Rule.builder("reversed", Effect.PERMIT).target(range(size, IntegerVal.of(20), IntegerVal.of(10), false))
						.obligation(ObligationExpression.builder("reversed", Effect.PERMIT))).build();
		for(List<Long> values : List.of(List.of(15L, 15L), List.of(32L), List.of(5L, 25L), List.of(45L))){
			RequestContext request = rangeRequest("size", values.stream().map(IntegerVal::of).toList());
			Result expected = decide(policy, repository(), mock(PolicyInformationPoint.class), request, "off", false);
			Result actual = decide(policy, repository(), mock(PolicyInformationPoint.class), request, "on", false);
			assertResponse(expected, actual);
			if(values.equals(List.of(5L, 25L))){
				assertEquals(Decision.PERMIT, actual.getDecision());
				assertEquals(Set.of("first", "overlap", "duplicate", "reversed"),
						Set.copyOf(actual.getObligations().stream().map(DecisionRuleResponse::getId).toList()));
			}
		}
	}

	@Test
	void dateBoundsKeepOffsetAndAbsentZoneComparisons() throws Exception {
		AttributeDesignatorKey day = rangeKey("day", XacmlTypes.DATE);
		Policy policy = rangePolicy("dates", "1.0", range(day, XacmlTypes.DATE.ofAny("2024-06-15"),
				XacmlTypes.DATE.ofAny("2024-06-17Z"), false));
		for(String value : List.of("2024-06-14Z", "2024-06-15+02:00", "2024-06-15",
				"2024-06-16+02:00", "2024-06-17+02:00", "2024-06-17Z")){
			Decision expected = value.equals("2024-06-15") || value.equals("2024-06-16+02:00")
					|| value.equals("2024-06-17+02:00")
					? Decision.DENY : Decision.PERMIT;
			assertRangeModes(policy, repository(), mock(PolicyInformationPoint.class),
					rangeRequest("day", List.of(XacmlTypes.DATE.ofAny(value))), expected);
		}
	}

	@Test
	void absentRequiredAndPipValuesKeepTheirDecisionAndStatus() throws Exception {
		AttributeDesignatorKey size = rangeKey("size", XacmlTypes.INTEGER);
		RequestContext absent = RequestContext.builder().returnPolicyIdList(true).build();
		Policy optional = rangePolicy("optional-range", "1.0", range(size, IntegerVal.of(10), IntegerVal.of(20), false));
		Policy required = rangePolicy("required-range", "1.0", range(size, IntegerVal.of(10), IntegerVal.of(20), true));
		PolicyInformationPoint empty = mock(PolicyInformationPoint.class);
		assertRangeModes(optional, repository(), empty, absent, Decision.PERMIT);
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE,
				assertRangeModes(required, repository(), empty, absent, Decision.INDETERMINATE_D)
						.getStatus().getStatusCode().getValue());
		PolicyInformationPoint supplied = mock(PolicyInformationPoint.class);
		when(supplied.resolve(any(EvaluationContext.class), eq(size)))
				.thenReturn(Optional.of(XacmlTypes.INTEGER.bagOf(IntegerVal.of(15))));
		assertRangeModes(optional, repository(), supplied, absent, Decision.DENY);
		RequestContext otherAttribute = rangeRequest("other", List.of(StringVal.of("unrelated")));
		assertRangeModes(optional, repository(), supplied, otherAttribute, Decision.DENY);
	}

	@Test
	void repositoryReplacementUsesTheNewRangeRules() throws Exception {
		AttributeDesignatorKey size = rangeKey("size", XacmlTypes.INTEGER);
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("range-replacement",
				FunctionProvider.builder().withDefaultFunctions().build(), ALGORITHMS);
		Field executor = AbstractPolicyRepository.class.getDeclaredField("notificationExecutor");
		executor.setAccessible(true);
		executor.set(repository, (java.util.concurrent.Executor)Runnable::run);
		repository.add(rangePolicy("entry", "1.0", range(size, IntegerVal.of(10), IntegerVal.of(20), false)));
		PolicySet root = set("range-root", "1.0:first-applicable", List.of(PolicyIDReference.builder("entry").build()));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		RequestContext request = rangeRequest("size", List.of(IntegerVal.of(15)));
		try(PolicyDecisionPoint off = PolicyDecisionPointBuilder.builder("range-off").rootPolicy(root)
				.policyRepository(repository).pip(pip).build();
				PolicyDecisionPoint on = PolicyDecisionPointBuilder.builder("range-on").rootPolicy(root)
						.policyRepository(repository).pip(pip).targetIndex().build()){
			Result before = off.decide(request).getResults().iterator().next();
			assertEquals(Decision.DENY, before.getDecision());
			assertResponse(before, on.decide(request).getResults().iterator().next());
			repository.add(rangePolicy("entry", "2.0", range(size, IntegerVal.of(20), IntegerVal.of(30), false)));
			Result after = off.decide(request).getResults().iterator().next();
			assertEquals(Decision.PERMIT, after.getDecision());
			assertResponse(after, on.decide(request).getResults().iterator().next());
		}
	}

	@Test
	void earlierIndeterminateAndUnexpectedValueKeepTheOriginalStatus() throws Exception {
		AttributeDesignatorKey size = rangeKey("size", XacmlTypes.INTEGER);
		Target later = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("yes"), designator(key("missing"), true), EQUAL)
				.match(IntegerVal.of(20), designator(size, false), rangeFunction("integer", "less-than-or-equal"))
				.build()).build()).build();
		Policy policy = Policy.builder("status-range").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:deny-overrides"))
				.rule(Rule.builder("broken", Effect.PERMIT).target(target(key("absent"), true, EQUAL, "yes")))
				.rule(Rule.builder("rejected", Effect.DENY).target(later))
				.rule(Rule.builder("indexed", Effect.DENY).target(range(size, IntegerVal.of(30), IntegerVal.of(40), false)))
				.build();
		RequestContext request = rangeRequest("size", List.of(IntegerVal.of(5)));
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE,
				assertRangeModes(policy, repository(), mock(PolicyInformationPoint.class), request, Decision.INDETERMINATE_P)
						.getStatus().getStatusCode().getValue());
		Value<?> wrongClass = new Value.OtherValue<Long>(XacmlTypes.INTEGER, 15L) { };
		Policy typeError = rangePolicy("type-error", "1.0", range(size, IntegerVal.of(10), IntegerVal.of(20), false));
		assertRangeModes(typeError, repository(), mock(PolicyInformationPoint.class),
				rangeRequest("size", List.of(wrongClass)), Decision.INDETERMINATE_D);
	}

	@Test
	void aLaterMatchCanMakeAContextFreeTargetNotApplicable() throws Exception {
		AttributeDesignatorKey other = key("other");
		Target target = Target.builder()
				.anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
						.match(StringVal.of("actual"), designator(KEY, false), EQUAL).build()).build())
				.anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
						.match(StringVal.of("actual"), designator(KEY, false), EQUAL)
						.match(StringVal.of("wanted"), designator(other, false), EQUAL).build()).build()).build();
		PolicySet root = set("later-match", "1.0:first-applicable", List.of(
				policy("unmatched", Effect.DENY, target), policy("fallback", Effect.PERMIT, null)));
		RequestContext request = RequestContext.builder().attributes(Category.resource().entity(Entity.builder()
				.attribute(Attribute.builder("resource").value(StringVal.of("actual")).build(),
						Attribute.builder("other").value(StringVal.of("different")).build()).build()).build()).build();
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result expected = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, expected.getDecision());
		assertEquals(StatusCodeId.OK, expected.getStatus().getStatusCode().getValue());
		assertResponse(expected, decide(root, repository(), pip, request, "observed", false));
		assertResponse(expected, decide(root, repository(), pip, request, "off", false));
		assertResponse(expected, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void selectorTargetUsesItsPolicyContext() throws Exception {
		AttributeSelector selector = AttributeSelector.builder().key(AttributeSelectorKey.builder()
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).xpath("/record/name/text()").build())
				.mustBePresent(true).build();
		Target selected = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("alice"), selector, EQUAL).build()).build()).build();
		PolicySet root = set("selector", "1.0:first-applicable", List.of(
				policy("unmatched", Effect.DENY, target(KEY, false, EQUAL, "other")),
				policy("selected", Effect.PERMIT, selected)));
		RequestContext request = RequestContext.builder().returnPolicyIdList(true).attributes(Category.resource()
				.entity(Entity.builder().content(XmlContent.of("<record><name>alice</name></record>"))
						.attribute(Attribute.builder("resource").value(StringVal.of("actual")).build()).build()).build()).build();
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result expected = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, expected.getDecision());
		assertEquals(StatusCodeId.OK, expected.getStatus().getStatusCode().getValue());
		assertEquals(List.of("selected-obligation"), expected.getObligations().stream().map(DecisionRuleResponse::getId).toList());
		assertEquals(List.of("selected-advice"), expected.getAssociatedAdvice().stream().map(DecisionRuleResponse::getId).toList());
		assertResponse(expected, decide(root, repository(), pip, request, "off", false));
		assertResponse(expected, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void pinnedReferencesKeepTheSameResponseAndOrder() throws Exception {
		Policy alpha = policy("alpha", Effect.PERMIT, target(KEY, false, EQUAL, "alpha"));
		Policy beta = policy("beta", Effect.PERMIT, target(KEY, false, EQUAL, "beta"));
		PolicyRepository repository = repository();
		repository.add(alpha);
		repository.add(beta);
		List<CompositeDecisionRule> children = List.of(
				PolicyIDReference.builder("alpha").build(), policy("other", Effect.PERMIT, target(KEY, false, EQUAL, "other")),
				PolicyIDReference.builder("beta").build());
		PolicySet root = set("references", "3.0:deny-overrides", children);
		RequestContext request = request(List.of("alpha", "beta"));
		PolicyInformationPoint pip = pip(KEY, "unused");
		Result baseline = decide(root, repository, pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertResponse(baseline, decide(root, repository, pip, request, "observed", false));
		assertResponse(baseline, decide(root, repository, pip, request, "off", false));
		assertResponse(baseline, decide(root, repository, pip, request, "on", false));
		assertEquals(children, List.copyOf(root.getDecisions()));
		assertTrue(root.contains(children.get(0)));
		RequestContext betaOnly = request(List.of("beta"));
		Result expected = decide(root, repository, pip, betaOnly, "baseline", false);
		assertEquals(Decision.PERMIT, expected.getDecision());
		assertResponse(expected, decide(root, repository, pip, betaOnly, "observed", false));
		assertResponse(expected, decide(root, repository, pip, betaOnly, "off", false));
		assertResponse(expected, decide(root, repository, pip, betaOnly, "on", false));
	}

	@Test
	void requiredAndDifferentKeysRemainCandidates() throws Exception {
		AttributeDesignatorKey missing = key("missing");
		PolicySet root = set("required", "3.0:deny-overrides", List.of(
				policy("other", Effect.PERMIT, target(KEY, false, EQUAL, "wanted")),
				policy("required", Effect.PERMIT, target(missing, true, EQUAL, "wanted"))));
		RequestContext request = request(List.of("different"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.INDETERMINATE_P, baseline.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, baseline.getStatus().getStatusCode().getValue());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void laterMatchesAndAlternativeBranchesKeepTheirStatusOrder() throws Exception {
		AttributeDesignatorKey missing = key("missing");
		MatchAllOf conjunction = MatchAllOf.builder()
				.match(StringVal.of("wanted"), designator(KEY, false), EQUAL)
				.match(StringVal.of("yes"), designator(missing, true), EQUAL).build();
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(conjunction).build()).build();
		PolicySet root = set("match-order", "1.0:first-applicable", List.of(policy("child", Effect.PERMIT, target)));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		for(String value : List.of("different", "wanted")){
			RequestContext request = request(List.of(value));
			Result baseline = decide(root, repository(), pip, request, "baseline", false);
			assertEquals(value.equals("wanted") ? Decision.INDETERMINATE_P : Decision.NOT_APPLICABLE, baseline.getDecision());
			assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
			assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
		}
		Target alternatives = Target.builder().anyOf(MatchAnyOf.builder()
				.anyOf(MatchAllOf.builder().match(StringVal.of("wanted"), designator(KEY, false), EQUAL).build())
				.anyOf(MatchAllOf.builder().match(StringVal.of("yes"), designator(missing, true), EQUAL).build()).build()).build();
		PolicySet withAlternative = set("alternatives", "1.0:first-applicable",
				List.of(policy("child", Effect.PERMIT, alternatives)));
		RequestContext request = request(List.of("different"));
		Result baseline = decide(withAlternative, repository(), pip, request, "baseline", false);
		assertEquals(Decision.INDETERMINATE_P, baseline.getDecision());
		assertResponse(baseline, decide(withAlternative, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(withAlternative, repository(), pip, request, "on", false));
	}

	@Test
	void anOptionalDifferentKeyCanStillComeFromTheInformationPoint() throws Exception {
		AttributeDesignatorKey other = key("other");
		PolicySet root = set("different-key", "3.0:deny-overrides", List.of(
				policy("ordinary", Effect.DENY, target(KEY, false, EQUAL, "wanted")),
				policy("external", Effect.PERMIT, target(other, false, EQUAL, "supplied"))));
		PolicyInformationPoint pip = pip(other, "supplied");
		RequestContext request = request(List.of("different"));
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertEquals(List.of("other"), baseline.getResolvedAttributes().stream()
				.flatMap(category -> category.getEntity().attributes().stream()).map(Attribute::getAttributeId).toList());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void aCustomStringValueKeepsItsNormalMatchBehavior() throws Exception {
		Value<?> custom = new Value.OtherValue<String>(XacmlTypes.STRING, "wanted") { };
		RequestContext request = RequestContext.builder().returnPolicyIdList(true).attributes(Category.resource()
				.entity(Entity.builder().attribute(Attribute.builder("resource").value(custom).build()).build()).build()).build();
		PolicySet root = set("custom-value", "1.0:first-applicable", List.of(
				policy("wanted", Effect.PERMIT, target(KEY, false, EQUAL, "wanted"))));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.INDETERMINATE_P, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void theHandlerContractDecidesWhetherTheEntityOrItsResolutionAnswersATarget(){
		PolicySet root = set("handler", "1.0:first-applicable", List.of(
				policy("wanted", Effect.PERMIT, target(KEY, false, EQUAL, "wanted")),
				policy("fallback", Effect.DENY, null)));
		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(context(handler(false)))));
		assertEquals(Decision.DENY, root.evaluate(root.createContext(context(handler(true)))));
	}

	/** A handler whose entity holds a value the target does not match, and whose resolution answers one it does */
	private static EvaluationContextHandler handler(boolean usesRequestEntityForDesignators){
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class, org.mockito.Mockito.CALLS_REAL_METHODS);
		when(handler.usesRequestEntityForDesignators()).thenReturn(usesRequestEntityForDesignators);
		when(handler.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(Entity.builder()
				.attribute(Attribute.builder("resource").value(StringVal.of("different")).build()).build()));
		when(handler.resolve(any(EvaluationContext.class), eq(KEY)))
				.thenReturn(Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("wanted"))));
		return handler;
	}

	private static RootEvaluationContext context(EvaluationContextHandler handler){
		return new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO, XPathVersion.XPATH1,
				mock(PolicyReferenceResolver.class), handler, true, PreparedPolicySets.create(false));
	}

	@Test
	void requestResolutionEffectsKeepTheDecisionCacheTtl(){
		PolicySet root = set("ttl", "1.0:first-applicable", List.of(
				policy("unmatched", Effect.DENY, target(KEY, false, EQUAL, "wanted")),
				policy("fallback", Effect.PERMIT, null)));
		// A handler outside the contract of usesRequestEntityForDesignators keeps the effects of its resolution
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class, org.mockito.Mockito.CALLS_REAL_METHODS);
		when(handler.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(Entity.builder()
				.attribute(Attribute.builder("resource").value(StringVal.of("different")).build()).build()));
		when(handler.resolve(any(EvaluationContext.class), eq(KEY))).thenAnswer(call -> {
			EvaluationContext context = call.getArgument(0);
			context.setDecisionCacheTTL(Duration.ofSeconds(5));
			return Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("different")));
		});
		RootEvaluationContext context = new RootEvaluationContext(Clock.systemUTC(), false, Duration.ofSeconds(30),
				XPathVersion.XPATH1, mock(PolicyReferenceResolver.class), handler, true, PreparedPolicySets.create(false));
		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(context)));
		assertEquals(Duration.ofSeconds(5), context.getDecisionCacheTTL());
	}

	@ParameterizedTest
	@MethodSource("requests")
	void anObservedDecisionMatchesEveryTargetInItsOwnContextAndAgrees(List<String> values, Decision expected,
			boolean validate) throws Exception {
		List<CompositeDecisionRule> roots = List.of(
				set("root", "3.0:deny-overrides", List.of(
						policy("permit", Effect.PERMIT, target(KEY, false, EQUAL, "alpha", "beta")),
						policy("deny", Effect.DENY, target(KEY, false, EQUAL, "gamma")))),
				Policy.builder("rules").version("1.0")
						.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:deny-overrides"))
						.rule(Rule.builder("permit", Effect.PERMIT).target(target(KEY, false, EQUAL, "alpha", "beta")))
						.rule(Rule.builder("deny", Effect.DENY).target(target(KEY, false, EQUAL, "gamma"))).build());
		PolicyInformationPoint pip = pip(KEY, "alpha");
		RequestContext request = request(values);
		for(CompositeDecisionRule root : roots){
			Result unobserved = decide(root, repository(), pip, request, "off", validate);
			assertEquals(expected, unobserved.getDecision());
			assertResponse(unobserved, decide(root, repository(), pip, request, "observed", validate));
		}
	}

	@Test
	void aMissingAttributeBeforeANonMatchingMatchKeepsTheStatusOfTheDecision() throws Exception {
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("yes"), designator(key("missing"), true), EQUAL)
				.match(StringVal.of("other"), designator(KEY, false), EQUAL).build()).build()).build();
		PolicySet root = set("status", "3.0:deny-overrides", List.of(
				policy("broken", Effect.PERMIT, target(key("absent"), true, EQUAL, "yes")),
				policy("unmatched", Effect.DENY, target)));
		RequestContext request = request(List.of("actual"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.INDETERMINATE_P, baseline.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, baseline.getStatus().getStatusCode().getValue());
		assertResponse(baseline, decide(root, repository(), pip, request, "observed", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void aForeignEvaluationRootUsesItsOwnAttributeResolution(){
		PolicySet root = set("foreign", "1.0:first-applicable", List.of(
				policy("wanted", Effect.PERMIT, target(KEY, false, EQUAL, "wanted"))));
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class, org.mockito.Mockito.CALLS_REAL_METHODS);
		when(handler.getEntity(CategoryId.RESOURCE)).thenReturn(Optional.of(Entity.builder()
				.attribute(Attribute.builder("resource").value(StringVal.of("different")).build()).build()));
		when(handler.resolve(any(EvaluationContext.class), eq(KEY)))
				.thenReturn(Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("wanted"))));
		RootEvaluationContext direct = new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO,
				XPathVersion.XPATH1, mock(PolicyReferenceResolver.class), handler, true, PreparedPolicySets.create(true));
		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(direct)));
		RootEvaluationContext delegate = new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO,
				XPathVersion.XPATH1, mock(PolicyReferenceResolver.class), handler, true, PreparedPolicySets.create(true));
		EvaluationContext foreign = mock(EvaluationContext.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(delegate)));
		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(foreign)));
		assertEquals(List.of("wanted-obligation"), delegate.getMatchingObligations(Decision.PERMIT).stream()
				.map(DecisionRuleResponse::getId).toList());
		assertEquals(List.of("wanted-advice"), delegate.getMatchingAdvices(Decision.PERMIT).stream()
				.map(DecisionRuleResponse::getId).toList());
	}

	@Test
	void aCustomCompositeRuleKeepsItsOwnMatchBehavior() throws Exception {
		Target target = target(KEY, false, EQUAL, "wanted");
		PolicySet root = set("extension", "1.0:first-applicable", List.of(
				policy("ordinary", Effect.DENY, target), new IndexCustomChild(target)));
		RequestContext request = request(List.of("different"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void aContextWhoseEntityLookupFailsKeepsItsNormalResolution(){
		PolicySet root = set("entity-failure", "1.0:first-applicable", List.of(
				policy("wanted", Effect.PERMIT, target(KEY, false, EQUAL, "wanted"))));
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class);
		when(handler.getEntity(CategoryId.RESOURCE)).thenThrow(new IllegalStateException("entity unavailable"));
		when(handler.resolve(any(EvaluationContext.class), eq(KEY)))
				.thenReturn(Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("wanted"))));
		RootEvaluationContext context = new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO,
				XPathVersion.XPATH1, mock(PolicyReferenceResolver.class), handler, true, PreparedPolicySets.create(true));
		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(context)));
		assertEquals(List.of("wanted-obligation"), context.getMatchingObligations(Decision.PERMIT).stream()
				.map(DecisionRuleResponse::getId).toList());
	}

	@Test
	void customPredicatesAndTargetSubclassesRemainCandidates() throws Exception {
		FunctionSpec custom = mock(FunctionSpec.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(EQUAL)));
		doReturn(BagStorage.Scan.NONE).when(custom).scan(any(), any(), any());
		doReturn(BooleanVal.of(true)).when(custom).invoke(any(EvaluationContext.class), any(Expression.class), any(Expression.class));
		Target customTarget = new Target(Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("wanted"), designator(KEY, false), EQUAL).build()).build())) {
			@Override public MatchResult match(EvaluationContext context){ return MatchResult.MATCH; }
		};
		PolicySet root = set("custom", "3.0:deny-overrides", List.of(
				policy("ordinary", Effect.PERMIT, target(KEY, false, EQUAL, "wanted")),
				policy("function", Effect.PERMIT, target(KEY, false, custom, "wanted")),
				policy("subclass", Effect.PERMIT, customTarget)));
		RequestContext request = request(List.of("different"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void anUnvisitedCustomLiteralIsNotConsultedDuringPolicyConstruction() throws Exception {
		Value<?> literal = mock(Value.class);
		when(literal.getEvaluatesTo()).thenReturn(XacmlTypes.STRING);
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(literal, designator(KEY, false), EQUAL).build()).build()).build();
		doThrow(new IllegalStateException("unvisited literal")).when(literal).getEvaluatesTo();
		PolicySet root = set("custom-literal", "1.0:first-applicable", List.of(
				policy("first", Effect.PERMIT, null), policy("unvisited", Effect.DENY, target)));
		Result result = decide(root, repository(), mock(PolicyInformationPoint.class), request(List.of("value")), "off", false);
		assertEquals(Decision.PERMIT, result.getDecision());
	}

	@Test
	void anUnvisitedCustomPredicateCannotFailDuringPreparation() throws Exception {
		FunctionSpec custom = mock(FunctionSpec.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(EQUAL)));
		Target target = target(KEY, false, custom, "wanted");
		doThrow(new IllegalStateException("unvisited predicate")).when(custom).getId();
		PolicySet root = set("unvisited", "1.0:first-applicable", List.of(
				policy("first", Effect.PERMIT, target(KEY, false, EQUAL, "alpha")),
				policy("later", Effect.DENY, target)));
		RequestContext request = request(List.of("alpha"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	@Test
	void positionalAndCustomAlgorithmsReceiveTheirOriginalChildren() throws Exception {
		List<CompositeDecisionRule> children = List.of(
				policy("first", Effect.PERMIT, target(KEY, false, EQUAL, "other")),
				policy("second", Effect.DENY, target(KEY, false, EQUAL, "alpha")),
				policy("third", Effect.PERMIT, target(KEY, false, EQUAL, "alpha")));
		PolicySet positional = set("positional", "3.0:on-permit-apply-second", children);
		DecisionCombiningAlgorithm<CompositeDecisionRule> custom = new DecisionCombiningAlgorithm<>() {
			@Override public String getId(){ return "custom"; }
			@Override public Decision combine(EvaluationContext context, List<CompositeDecisionRule> entries){
				return entries.size() == 3 && entries.get(0).getId().equals("first") ? Decision.PERMIT : Decision.DENY;
			}
		};
		PolicySet customSet = PolicySet.builder("custom-algorithm").version("1.0")
				.withCombiningAlgorithm(custom).compositeDecisionRules(children).build();
		PolicySet customSubclass = PolicySet.builder("custom-subclass").version("1.0")
				.withCombiningAlgorithm(new OnPermitApplySecondPolicyCombiningAlgorithm() {
					@Override public Decision combine(EvaluationContext context, List<CompositeDecisionRule> entries){
						return entries.size() == 3 ? Decision.PERMIT : Decision.DENY;
					}
				}).compositeDecisionRules(children).build();
		RequestContext request = request(List.of("alpha"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		for(PolicySet root : List.of(positional, customSet, customSubclass)){
			Result baseline = decide(root, repository(), pip, request, "baseline", false);
			assertEquals(Decision.PERMIT, baseline.getDecision());
			assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
			assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
		}
	}

	@Test
	void equalPolicySetsAndPoliciesKeepTheirOwnBehavior() throws Exception {
		PolicySet first = set("same-set", "1.0:first-applicable", List.of(policyWithRule(equalFunction(false))));
		PolicySet second = set("same-set", "1.0:first-applicable", List.of(policyWithRule(equalFunction(true))));
		assertEquals(first, second);
		PolicySet root = set("root", "1.0:first-applicable", List.of(first, second));
		RequestContext request = request(List.of("different"));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		Result baseline = decide(root, repository(), pip, request, "baseline", false);
		assertEquals(Decision.PERMIT, baseline.getDecision());
		assertResponse(baseline, decide(root, repository(), pip, request, "off", false));
		assertResponse(baseline, decide(root, repository(), pip, request, "on", false));
	}

	private static FunctionSpec equalFunction(boolean result){
		FunctionInvocation invocation = mock(FunctionInvocation.class);
		when(invocation.invoke(any(), any(), any(List.class))).thenReturn(BooleanVal.of(result));
		return FunctionSpecBuilder.builder(EQUAL.getId()).param(XacmlTypes.STRING).param(XacmlTypes.STRING)
				.build(XacmlTypes.BOOLEAN, invocation);
	}

	private static Policy policyWithRule(FunctionSpec function){
		return Policy.builder("same-policy").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("rule", Effect.PERMIT).target(target(KEY, false, function, "wanted")))
				.obligation(ObligationExpression.builder("same-obligation", Effect.PERMIT).attribute("origin", StringVal.of("same")))
				.build();
	}

	private static AttributeDesignatorKey key(String id){
		return AttributeDesignatorKey.builder(id).category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build();
	}

	private static AttributeDesignatorKey rangeKey(String id, XacmlTypes type){
		return AttributeDesignatorKey.builder(id).category(CategoryId.RESOURCE).dataType(type).build();
	}

	private static FunctionSpec rangeFunction(String type, String comparison){
		return FunctionProvider.builder().withDefaultFunctions().build()
				.getFunction(URN + "1.0:function:" + type + "-" + comparison).orElseThrow();
	}

	private static MatchAllOf bounds(AttributeDesignatorKey key, Value<?> lower, Value<?> upper, boolean required){
		String type = key.getDataType() == XacmlTypes.INTEGER ? "integer" : "date";
		return MatchAllOf.builder()
				.match(lower, designator(key, required), rangeFunction(type, "less-than-or-equal"))
				.match(upper, designator(key, required), rangeFunction(type, "greater-than")).build();
	}

	private static Target range(AttributeDesignatorKey key, Value<?> lower, Value<?> upper, boolean required){
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(bounds(key, lower, upper, required)).build()).build();
	}

	private static Policy rangePolicy(String id, String version, Target target){
		return Policy.builder(id).version(version)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("bounded", Effect.DENY).target(target)
						.obligation(ObligationExpression.builder("bounded", Effect.DENY)))
				.rule(Rule.builder("fallback", Effect.PERMIT)
						.obligation(ObligationExpression.builder("fallback", Effect.PERMIT))).build();
	}

	private static RequestContext rangeRequest(String id, List<? extends Value<?>> values){
		Attribute.Builder attribute = Attribute.builder(id);
		for(Value<?> value : values) attribute.value(value);
		return RequestContext.builder().returnPolicyIdList(true).attributes(Category.resource().entity(Entity.builder()
				.attribute(attribute.build()).build()).build()).build();
	}

	private static Result assertRangeModes(Policy policy, PolicyRepository repository, PolicyInformationPoint pip,
			RequestContext request, Decision expected) throws Exception {
		Result baseline = decide(policy, repository, pip, request, "off", false);
		assertEquals(expected, baseline.getDecision());
		assertResponse(baseline, decide(policy, repository, pip, request, "on", false));
		assertResponse(baseline, decide(policy, repository, pip, request, "observed-index", false));
		assertResponse(decide(policy, repository, pip, request, "off", true),
				decide(policy, repository, pip, request, "on", true));
		return baseline;
	}

	private static AttributeDesignator designator(AttributeDesignatorKey key, boolean required){
		return AttributeDesignator.builder().key(key).mustBePresent(required).build();
	}

	private static Target target(AttributeDesignatorKey key, boolean required, FunctionSpec function, String... values){
		MatchAnyOf.Builder alternatives = MatchAnyOf.builder();
		for(String value : values){
			alternatives.anyOf(MatchAllOf.builder().match(StringVal.of(value), designator(key, required), function).build());
		}
		return Target.builder().anyOf(alternatives.build()).build();
	}

	private static Policy policy(String id, Effect effect, Target target){
		return Policy.builder(id).version("1.0").target(target)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", effect))
				.obligation(ObligationExpression.builder(id + "-obligation", effect).attribute("origin", StringVal.of(id)))
				.advice(AdviceExpression.builder(id + "-advice", effect).attribute("origin", StringVal.of(id))).build();
	}

	private static PolicySet set(String id, String algorithm, List<CompositeDecisionRule> children){
		String[] parts = algorithm.split(":", 2);
		return PolicySet.builder(id).version("1.0").withCombiningAlgorithm(
				ALGORITHMS.getPolicyAlgorithm(URN + parts[0] + ":policy-combining-algorithm:" + parts[1]))
				.compositeDecisionRules(children).build();
	}

	private static PolicyRepository repository(){
		return PolicyRepository.build("target-index").build();
	}

	private static PolicyInformationPoint pip(AttributeDesignatorKey key, String value){
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(EvaluationContext.class), eq(key)))
				.thenReturn(Optional.of(XacmlTypes.STRING.bagOf(StringVal.of(value))));
		return pip;
	}

	private static RequestContext request(List<String> values){
		RequestContext.Builder builder = RequestContext.builder().returnPolicyIdList(true);
		if(!values.isEmpty()){
			Attribute.Builder attribute = Attribute.builder("resource");
			for(String value : values) attribute.value(StringVal.of(value));
			builder.attributes(Category.resource().entity(Entity.builder().attribute(attribute.build()).build()).build());
		}
		return builder.build();
	}

	/**
	 * Decides a request: "baseline" with the plain factory, "off" and "on" with the target index off
	 * and on, and "observed" with an observer, which matches every target in its own context
	 */
	private static Result decide(CompositeDecisionRule root, PolicyRepository repository, PolicyInformationPoint pip,
			RequestContext request, String mode, boolean validate) throws Exception {
		PolicyDecisionPoint pdp;
		if(!validate && !mode.equals("baseline")){
			PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("indexed-test")
					.rootPolicy(root).policyRepository(repository).pip(pip);
			if(mode.equals("on") || mode.equals("observed-index")) builder.targetIndex();
			if(mode.equals("observed") || mode.equals("observed-index")) builder.observer(OBSERVED);
			pdp = builder.build();
		}else{
			PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory factory = mode.equals("baseline")
					? new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
							new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(), XPathProvider.defaultProvider(),
							pip, new RequestContextHandlerChain(List.of()))
					: new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
							new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(), XPathProvider.defaultProvider(),
							pip, new RequestContextHandlerChain(List.of()), PreparedPolicySets.create(mode.equals("on")));
			if(validate){
				Field field = factory.getClass().getDeclaredField("validateFuncParamsAtRuntime");
				field.setAccessible(true);
				field.setBoolean(factory, true);
			}
			pdp = new DefaultPolicyDecisionPoint("baseline", new MetricRegistry(), factory);
		}
		return pdp.decide(request).getResults().iterator().next();
	}

	private static void assertResponse(Result expected, Result actual){
		assertEquals(expected, actual);
		assertEquals(expected.getStatus().getError().map(TargetIndexPdpTest::errorChain).orElse(List.of()),
				actual.getStatus().getError().map(TargetIndexPdpTest::errorChain).orElse(List.of()));
		assertResponses(expected.getObligations(), actual.getObligations());
		assertResponses(expected.getAssociatedAdvice(), actual.getAssociatedAdvice());
		assertEquals(List.copyOf(expected.getPolicyIdentifiers()), List.copyOf(actual.getPolicyIdentifiers()));
	}

	private static void assertResponses(Collection<? extends DecisionRuleResponse> expected,
			Collection<? extends DecisionRuleResponse> actual){
		List<? extends DecisionRuleResponse> left = List.copyOf(expected);
		List<? extends DecisionRuleResponse> right = List.copyOf(actual);
		assertEquals(left, right);
		for(int i = 0; i < left.size(); i++){
			assertEquals(left.get(i).getFulfillOn(), right.get(i).getFulfillOn());
			assertEquals(List.copyOf(left.get(i).getAttributes()), List.copyOf(right.get(i).getAttributes()));
		}
	}

	private static List<String> errorChain(Throwable error){
		java.util.ArrayList<String> chain = new java.util.ArrayList<>();
		for(Throwable cause = error; cause != null; cause = cause.getCause()){
			chain.add(cause.getClass().getName() + ":" + cause.getMessage());
		}
		return chain;
	}
}
