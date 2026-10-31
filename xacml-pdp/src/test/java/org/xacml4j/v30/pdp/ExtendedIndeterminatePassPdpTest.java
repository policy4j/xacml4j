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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

class ExtendedIndeterminatePassPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();

	@Test
	void aPassEvaluatesTheResponsesOfItsChildrenAndReturnsNone(){
		Rule rule = Rule.builder("permit-rule", Effect.PERMIT)
				.advice(AdviceExpression.builder("rule-advice", Effect.PERMIT)
						.attribute(assignment("rule-response"))).build();
		Policy child = policy("child", rule)
				.obligation(ObligationExpression.builder("child-obligation", Effect.PERMIT)
						.attribute(assignment("policy-response"))).build();
		PolicySet root = set("root", "1.0:first-applicable").target(target("outer-target")).policy(child).build();
		Result result = decide(root, pipWithResponses());

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		assertTrue(result.getStatus().getMessage().orElseThrow().contains("outer-target"));
		assertEquals(Set.of("rule-response", "policy-response"), resolvedAttributeIds(result));
		assertTrue(result.getObligations().isEmpty());
		assertTrue(result.getAssociatedAdvice().isEmpty());
	}

	@Test
	void aNestedIndeterminateTargetStillComputesItsOwnExtendedValue(){
		Policy child = policy("child", Rule.builder("permit-rule", Effect.PERMIT).build())
				.target(target("inner-target")).build();
		PolicySet root = set("root", "1.0:first-applicable").target(target("outer-target")).policy(child).build();

		Result result = decide(root, pipWithResponses());

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		assertTrue(result.getStatus().getMessage().orElseThrow().contains("outer-target"));
	}

	@Test
	void aTargetThatFailsInTheResolverKeepsItsStatusThroughThePass(){
		Policy child = policy("child", Rule.builder("permit-rule", Effect.PERMIT).build())
				.target(target("inner-target")).build();
		PolicySet root = set("root", "1.0:first-applicable").target(target("failing-target")).policy(child).build();

		Result result = decide(root, pipWithResponses());

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
	}

	@Test
	void aFailingObligationInThePassMakesItsPolicyIndeterminateAndKeepsTheStatus(){
		Rule deny = Rule.builder("deny-rule", Effect.DENY)
				.obligation(ObligationExpression.builder("deny-obligation", Effect.DENY)
						.attribute(assignment("unavailable-response"))).build();
		PolicySet root = set("root", "3.0:deny-overrides").target(target("outer-target"))
				.policy(policy("deny", deny).build())
				.policy(policy("permit", Rule.builder("permit-rule", Effect.PERMIT).build()).build()).build();

		Result result = decide(root, pipWithResponses());

		// deny-overrides(Indeterminate{D}, Permit), XACML 3.0 sections 7.14 and 7.18
		assertEquals(Decision.INDETERMINATE_DP, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		assertTrue(result.getStatus().getMessage().orElseThrow().contains("outer-target"));
		assertTrue(result.getResolvedAttributes().isEmpty());
	}

	@Test
	void aFailingObligationBelowAnIndeterminateTargetKeepsTheDecisionIndeterminate(){
		Rule permit = Rule.builder("p1-rule", Effect.PERMIT)
				.obligation(ObligationExpression.builder("p1-obligation", Effect.PERMIT)
						.attribute(assignment("unavailable-response"))).build();
		PolicySet indeterminate = set("cs", "3.0:permit-overrides").target(target("outer-target"))
				.policy(policy("p1", permit).build())
				.policy(policy("p2", Rule.builder("p2-rule", Effect.DENY).build()).build()).build();
		PolicySet root = set("root", "3.0:deny-overrides")
				.policySet(indeterminate)
				.policy(policy("s", Rule.builder("s-rule", Effect.PERMIT).build()).build()).build();

		Result result = decide(root, pipWithResponses());

		// The set could have been Deny: permit-overrides(Indeterminate{P}, Deny) is Indeterminate{DP}
		assertEquals(Decision.INDETERMINATE_DP, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
	}

	@Test
	void theChildrenOfAPassAreNotInThePolicyIdentifierList(){
		PolicySet indeterminate = set("cs", "1.0:first-applicable").target(target("outer-target"))
				.policy(policy("p", Rule.builder("p-rule", Effect.PERMIT).build()).build()).build();
		PolicySet root = set("root", "3.0:deny-overrides")
				.policySet(indeterminate)
				.policy(policy("s", Rule.builder("s-rule", Effect.PERMIT).build()).build()).build();

		Result result = decide(root, pipWithResponses());

		assertEquals(Decision.PERMIT, result.getDecision());
		Set<String> ids = result.getPolicyIdentifiers().stream()
				.map(CompositeDecisionRuleIDReference::getId)
				.collect(Collectors.toSet());
		assertTrue(ids.contains("s"), () -> "ids=" + ids);
		assertFalse(ids.contains("p"), () -> "ids=" + ids);
	}

	private static Set<String> resolvedAttributeIds(Result result){
		return result.getResolvedAttributes().stream()
				.flatMap(category -> category.getEntity().attributes().stream())
				.map(Attribute::getAttributeId)
				.collect(Collectors.toSet());
	}

	private static Result decide(PolicySet root, PolicyInformationPoint pip){
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("extended-pass")
				.policyRepository(PolicyRepository.build("extended-pass").build()).rootPolicy(root).pip(pip).build();
		return pdp.decide(RequestContext.builder().returnPolicyIdList(true).build()).getResults().iterator().next();
	}

	private static PolicyInformationPoint pipWithResponses(){
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(EvaluationContext.class), any(AttributeDesignatorKey.class))).thenAnswer(call -> {
			AttributeDesignatorKey key = call.getArgument(1);
			if(key.getAttributeId().startsWith("failing")){
				throw new IllegalStateException("resolver failure");
			}
			return key.getAttributeId().endsWith("-response") && !key.getAttributeId().startsWith("unavailable")
					? Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("supplied"))) : Optional.empty();
		});
		return pip;
	}

	private static AttributeAssignmentExpression assignment(String id){
		return AttributeAssignmentExpression.builder(id).expression(designator(id)).build();
	}

	private static AttributeDesignator designator(String id){
		return AttributeDesignator.builder().key(AttributeDesignatorKey.builder(id).category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING).build()).mustBePresent(true).build();
	}

	private static Target target(String id){
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("match"), designator(id),
						FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow())
				.build()).build()).build();
	}

	private static Policy.Builder policy(String id, Rule rule){
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(rule);
	}

	private static PolicySet.Builder set(String id, String algorithm){
		String[] parts = algorithm.split(":", 2);
		return PolicySet.builder(id).version("1.0").withCombiningAlgorithm(
				ALGORITHMS.getPolicyAlgorithm(URN + parts[0] + ":policy-combining-algorithm:" + parts[1]));
	}
}
