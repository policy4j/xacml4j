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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

class RuleTargetStatusPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final String INVALID_PATTERN = "Regular expression pattern is invalid";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final RequestContext REQUEST = RequestContext.builder()
			.attributes(Category.resource().entity(Entity.builder()
					.attribute(Attribute.builder("resource").value(StringVal.of("document")).build()).build()).build())
			.build();

	@ParameterizedTest
	@CsvSource({"false, false", "false, true", "true, false", "true, true"})
	void malformedRegexInRuleTargetReportsSyntaxError(boolean observed, boolean indexed){
		Rule rule = Rule.builder("rule", Effect.PERMIT).target(regexTarget()).build();
		Result result = decide(policy("policy", rule).build(), observed, indexed);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.SYNTAX_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(INVALID_PATTERN, result.getStatus().getMessage().orElseThrow());
	}

	@ParameterizedTest
	@CsvSource({"false, false", "false, true", "true, false", "true, true"})
	void missingRequiredRuleTargetAttributeReportsItsDetails(boolean observed, boolean indexed){
		Rule rule = Rule.builder("rule", Effect.PERMIT).target(missingTarget("absent")).build();
		Result result = decide(policy("policy", rule).build(), observed, indexed);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		String message = result.getStatus().getMessage().orElseThrow();
		assertTrue(message.contains("absent"));
		assertTrue(message.contains("DataType="));
	}

	@ParameterizedTest
	@CsvSource({"false, false", "false, true", "true, false", "true, true"})
	void malformedRegexInPolicyTargetKeepsItsStatus(boolean observed, boolean indexed){
		Policy root = policy("policy", Rule.builder("rule", Effect.PERMIT).build()).target(regexTarget()).build();
		Result result = decide(root, observed, indexed);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.SYNTAX_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(INVALID_PATTERN, result.getStatus().getMessage().orElseThrow());
	}

	@ParameterizedTest
	@CsvSource({"false, false", "false, true", "true, false", "true, true"})
	void ruleTargetFailureInPassKeepsTheStatusThatStartedIt(boolean observed, boolean indexed){
		Rule rule = Rule.builder("rule", Effect.PERMIT).target(missingTarget("inner-absent")).build();
		PolicySet root = PolicySet.builder("root").target(regexTarget())
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(policy("child", rule).build()).build();
		Result result = decide(root, observed, indexed);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.SYNTAX_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(INVALID_PATTERN, result.getStatus().getMessage().orElseThrow());
	}

	private static Policy.Builder policy(String id, Rule rule){
		return Policy.builder(id)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(rule);
	}

	private static Target regexTarget(){
		return target("(", "resource", "1.0:function:string-regexp-match");
	}

	private static Target missingTarget(String id){
		return target("match", id, "1.0:function:string-equal");
	}

	private static Target target(String literal, String id, String function){
		AttributeDesignator designator = AttributeDesignator.builder().key(AttributeDesignatorKey.builder(id)
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build()).mustBePresent(true).build();
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of(literal), designator, FUNCTIONS.getFunction(URN + function).orElseThrow())
				.build()).build()).build();
	}

	private static Result decide(CompositeDecisionRule root, boolean observed, boolean indexed){
		PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("rule-target-status")
				.policyRepository(PolicyRepository.build("rule-target-status").build())
				.pip(PolicyInformationPoint.builder("rule-target-status").build()).rootPolicy(root);
		if(observed) builder.observer((pdp, request) -> "request");
		if(indexed) builder.targetIndex();
		return builder.build().decide(REQUEST).getResults().iterator().next();
	}
}
