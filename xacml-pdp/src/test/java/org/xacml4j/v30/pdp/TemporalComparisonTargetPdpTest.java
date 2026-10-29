package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Policy Decision Point (PDP) Implementation
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

class TemporalComparisonTargetPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:1.0:";
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final DecisionObserver<Boolean> OBSERVED = (pdp, request) -> Boolean.TRUE;
	private static final List<String> COMPARISONS = List.of(
			"greater-than", "greater-than-or-equal", "less-than", "less-than-or-equal");

	static Stream<Arguments> boundaries(){
		Stream.Builder<Arguments> cases = Stream.builder();
		for(Temporal temporal : List.of(
				new Temporal("date", XacmlTypes.DATE, "2024-06-14", "2024-06-15", "2024-06-16+02:00"),
				new Temporal("dateTime", XacmlTypes.DATETIME, "2024-06-15T11:59:59.999999999",
						"2024-06-15T12:00:00", "2024-06-15T14:00:00.000000001+02:00"),
				new Temporal("time", XacmlTypes.TIME, "11:59:59.999999999Z",
						"12:00:00Z", "14:00:00.000000001+02:00"))){
			Value<?> before = temporal.type().ofAny(temporal.before());
			Value<?> equal = temporal.type().ofAny(temporal.equal());
			Value<?> after = temporal.type().ofAny(temporal.after());
			List<List<Value<?>>> bags = List.of(List.of(before), List.of(equal), List.of(after),
					List.of(before, equal), List.of(equal, after), List.of(before, after),
					List.of(before, before), List.of(after, after));
			// XACML 3.0 §§7.6–7.7, A.3.8: literal first, any successful bag element matches.
			boolean[][] matches = {
					{true, false, false, true, false, true, true, false},
					{true, true, false, true, true, true, true, false},
					{false, false, true, false, true, true, false, true},
					{false, true, true, true, true, true, false, true}
			};
			for(int comparison = 0; comparison < COMPARISONS.size(); comparison++){
				for(int bag = 0; bag < bags.size(); bag++){
					cases.add(Arguments.of(temporal.name() + "-" + COMPARISONS.get(comparison), equal,
							bags.get(bag), matches[comparison][bag] ? Decision.DENY : Decision.PERMIT));
				}
			}
		}
		return cases.build();
	}

	@ParameterizedTest
	@MethodSource("boundaries")
	void temporalTargetsCompareTheLiteralToEachBagValue(String function, Value<?> literal,
			List<Value<?>> values, Decision expected) throws Exception {
		Result result = assertDecisions(function, literal, values);
		assertEquals(expected, result.getDecision());
		assertTrue(result.getStatus().isSuccess());
	}

	static Stream<Arguments> timeZoneMismatches(){
		return COMPARISONS.stream().flatMap(comparison -> Stream.of(
				Arguments.of("time-" + comparison, XacmlTypes.TIME.ofAny("12:00:00Z"),
						List.of(XacmlTypes.TIME.ofAny("13:00:00"))),
				Arguments.of("time-" + comparison, XacmlTypes.TIME.ofAny("12:00:00"),
						List.of(XacmlTypes.TIME.ofAny("13:00:00Z"))),
				Arguments.of("time-" + comparison, XacmlTypes.TIME.ofAny("12:00:00Z"),
						List.of(XacmlTypes.TIME.ofAny("11:00:00Z"), XacmlTypes.TIME.ofAny("13:00:00")))));
	}

	@ParameterizedTest
	@MethodSource("timeZoneMismatches")
	void mixedTimeZonesKeepTheOrdinaryDecisionAndStatus(String function, Value<?> literal,
			List<Value<?>> values) throws Exception {
		assertDecisions(function, literal, values);
	}

	private static Result assertDecisions(String name, Value<?> literal, List<Value<?>> values)
			throws Exception {
		Result ordinary = ordinary(name, literal, values);
		for(String mode : List.of("observed", "unobserved", "indexed")){
			Result actual = decide(function(name), literal, values, mode);
			assertEquals(ordinary, actual);
			assertEquals(ordinary.getStatus().getError().map(TemporalComparisonTargetPdpTest::errors),
					actual.getStatus().getError().map(TemporalComparisonTargetPdpTest::errors));
		}
		return ordinary;
	}

	private static Result ordinary(String name, Value<?> literal, List<Value<?>> values) throws Exception {
		FunctionSpec invocation = mock(FunctionSpec.class,
				withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(function(name))));
		doReturn(BagStorage.Scan.NONE).when(invocation).scan(any(), any(), any());
		return decide(invocation, literal, values, "observed");
	}

	private static FunctionSpec function(String name){
		return FUNCTIONS.getFunction(URN + "function:" + name).orElseThrow();
	}

	private static Result decide(FunctionSpec function, Value<?> literal, List<Value<?>> values, String mode)
			throws Exception {
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("temporal").category(CategoryId.RESOURCE)
				.dataType(literal.getEvaluatesTo().getValueType()).build();
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(literal, AttributeDesignator.builder().key(key).mustBePresent(true).build(), function)
				.build()).build()).build();
		Policy policy = Policy.builder("temporal-policy").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("matching", Effect.DENY).target(target))
				.rule(Rule.builder("fallback", Effect.PERMIT)).build();
		Attribute.Builder attribute = Attribute.builder("temporal");
		for(Value<?> value : values) attribute.value(value);
		RequestContext request = RequestContext.builder().returnPolicyIdList(true)
				.attributes(Category.resource().entity(Entity.builder().attribute(attribute.build()).build()).build()).build();
		PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("temporal-" + mode)
				.policyRepository(PolicyRepository.build("temporal").build()).rootPolicy(policy)
				.pip(mock(PolicyInformationPoint.class));
		if(mode.equals("observed")) builder.observer(OBSERVED);
		if(mode.equals("indexed")) builder.targetIndex();
		try(PolicyDecisionPoint pdp = builder.build()){
			var results = pdp.decide(request).getResults();
			assertEquals(1, results.size());
			return results.iterator().next();
		}
	}

	private static List<String> errors(Throwable failure){
		List<String> chain = new ArrayList<>();
		for(Throwable cause = failure; cause != null; cause = cause.getCause()){
			chain.add(cause.getClass().getName() + ":" + cause.getMessage());
		}
		return chain;
	}

	private record Temporal(String name, XacmlTypes type, String before, String equal, String after) { }
}
