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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.WellKnownAttributeIds;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.FunctionSpec;
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
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * A key that no resolver of the information point supplies is a missing attribute, as an attempt
 * would find it: NotApplicable or Indeterminate with the missing-attribute status by MustBePresent;
 * a key that a resolver supplies is resolved (cycle AN of change optimize-pdp-evaluation)
 */
class UnresolvableAttributePdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final RequestContext REQUEST = RequestContext.builder()
			.attributes(Category.resource().entity(Entity.builder()
					.attribute(Attribute.builder("resource").value(StringVal.of("doc")).build()).build()).build())
			.build();

	@Test
	void anOptionalKeyWithoutAResolverDoesNotMatch(){
		PolicySet root = set(policy("unmatched", Effect.DENY, target("x", designator("absent", false))),
				policy("fallback", Effect.PERMIT, null));

		Result result = decide(root);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("fallback-obligation"),
				result.getObligations().stream().map(DecisionRuleResponse::getId).toList());
	}

	@Test
	void aRequiredKeyWithoutAResolverIsIndeterminateWithTheMissingAttribute(){
		PolicySet root = set(policy("required", Effect.DENY, target("x", designator("absent", true))));

		Result result = decide(root);

		assertEquals(Decision.INDETERMINATE_D, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of(), List.copyOf(result.getObligations()));
	}

	@Test
	void aKeyThatAResolverSuppliesIsResolved(){
		AttributeDesignator today = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder(WellKnownAttributeIds.CURRENT_DATE.getId())
						.category(CategoryId.ENVIRONMENT).dataType(XacmlTypes.DATE).build())
				.mustBePresent(true).build();
		Condition afterTheMillennium = Condition.condition(Apply.builder(function("1.0:function:date-greater-than"))
				.param(Apply.builder(function("1.0:function:date-one-and-only")).param(today).build(),
						XacmlTypes.DATE.ofAny("2000-01-01"))
				.build());
		PolicySet root = set(Policy.builder("dated").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("dated-rule", Effect.PERMIT).condition(afterTheMillennium)).build());

		Result result = decide(root);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
	}

	private static Result decide(CompositeDecisionRule root){
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("unresolvable")
				.policyRepository(PolicyRepository.build("unresolvable").build())
				.pip(PolicyInformationPoint.builder("defaults").build())
				.rootPolicy(root).build();
		return pdp.decide(REQUEST).getResults().iterator().next();
	}

	private static PolicySet set(CompositeDecisionRule... children){
		return PolicySet.builder("root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:first-applicable"))
				.compositeDecisionRules(List.of(children)).build();
	}

	private static Policy policy(String id, Effect effect, Target target){
		return Policy.builder(id).version("1.0").target(target)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", effect))
				.obligation(ObligationExpression.builder(id + "-obligation", effect).attribute("origin", StringVal.of(id)))
				.build();
	}

	private static Target target(String value, AttributeDesignator designator){
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of(value), designator, function("1.0:function:string-equal")).build()).build()).build();
	}

	private static AttributeDesignator designator(String id, boolean mustBePresent){
		return AttributeDesignator.builder().key(AttributeDesignatorKey.builder(id).category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING).build()).mustBePresent(mustBePresent).build();
	}

	private static FunctionSpec function(String id){
		return FUNCTIONS.getFunction(URN + id).orElseThrow();
	}
}
