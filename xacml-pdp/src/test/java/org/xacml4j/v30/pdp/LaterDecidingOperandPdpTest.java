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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Maintainer ruling 2026-10-07 (run 061 D11, bead policy4j-the.4.12) at the
 * decision level: a condition whose {@code or} or {@code and} has a deciding
 * operand is not Indeterminate because of another operand, and the status of
 * an Indeterminate decision is the status of the failure that decided it.
 * <p>
 * Operands: {@code M} is a required attribute that is missing (fails with
 * missing-attribute and records that status in the context); {@code E} is a
 * division by zero (fails with processing-error).
 */
class LaterDecidingOperandPdpTest
{
	private static final String F = "urn:oasis:names:tc:xacml:1.0:function:";

	@Test
	void orWithAMissingRequiredAttributeAndATrueOperandPermits()
	{
		assertDecisionAndStatus(decide(apply("or", missing(), TRUE())), Decision.PERMIT, StatusCodeId.OK);
		assertDecisionAndStatus(decide(apply("or", TRUE(), missing())), Decision.PERMIT, StatusCodeId.OK);
	}

	@Test
	void andWithAMissingRequiredAttributeAndAFalseOperandIsNotApplicable()
	{
		assertDecisionAndStatus(decide(apply("and", missing(), FALSE())), Decision.NOT_APPLICABLE, StatusCodeId.OK);
		assertDecisionAndStatus(decide(apply("and", FALSE(), missing())), Decision.NOT_APPLICABLE, StatusCodeId.OK);
	}

	@Test
	void withoutADecidingOperandTheStatusIsTheStatusOfTheFailedOperand()
	{
		assertDecisionAndStatus(decide(apply("or", missing(), FALSE())),
				Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE);
		assertDecisionAndStatus(decide(apply("and", divisionByZero(), TRUE())),
				Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	/**
	 * rv081 round 1: {@code and(or(M, T), E)}. The inner {@code or} is true,
	 * so the failure that decides is {@code E}. The missing-attribute status
	 * that {@code M} recorded in the context must not be the status of the
	 * decision.
	 */
	@Test
	void statusOfTheDecisionIsNotTheStatusOfAnOperandThatALaterValueOverruled()
	{
		Expression condition = apply("and", apply("or", missing(), TRUE()), divisionByZero());

		assertDecisionAndStatus(decide(condition), Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	/**
	 * rv081 round 2: a Permit rule with the condition {@code or(M, T)} and an
	 * obligation whose attribute assignment divides by zero. The condition
	 * is true although {@code M} recorded missing-attribute. The obligation
	 * then fails, and the decision reports the status of that failure.
	 */
	@Test
	void statusOfAFailedObligationIsNotTheStatusOfAnOperandThatALaterValueOverruled()
	{
		Rule.Builder rule = Rule.builder("rule", Effect.PERMIT)
				.condition(new Condition(apply("or", missing(), TRUE())))
				.obligation(ObligationExpression.builder("urn:test:obligation", Effect.PERMIT)
						.attribute(AttributeAssignmentExpression.builder("urn:test:assignment")
								.expression(apply("integer-divide",
										XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(0)))
								.build())
						.build());

		assertDecisionAndStatus(decide(rule), Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	@Test
	void statusOfAFailedAdviceIsNotTheStatusOfAnOperandThatALaterValueOverruled()
	{
		Rule.Builder rule = Rule.builder("rule", Effect.PERMIT)
				.condition(new Condition(apply("or", missing(), TRUE())))
				.advice(AdviceExpression.builder("urn:test:advice", Effect.PERMIT)
						.attribute(AttributeAssignmentExpression.builder("urn:test:assignment")
								.expression(apply("integer-divide",
										XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(0)))
								.build())
						.build());

		assertDecisionAndStatus(decide(rule), Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	@Test
	void nOfWithAMissingRequiredAttributeAndEnoughTrueOperandsPermits()
	{
		Expression condition = Apply.builder(F + "n-of", functions()::getFunction)
				.param(XacmlTypes.INTEGER.ofAny(2), missing(), TRUE(), TRUE())
				.build();

		assertDecisionAndStatus(decide(condition), Decision.PERMIT, StatusCodeId.OK);
	}

	private static Expression TRUE()
	{
		return BooleanVal.of(true);
	}

	private static Expression FALSE()
	{
		return BooleanVal.of(false);
	}

	/** {@code string-is-in("match", <required attribute that is not in the request>)}. */
	private static Expression missing()
	{
		AttributeDesignator designator = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder("urn:test:attribute")
						.category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.STRING)
						.build())
				.mustBePresent(true)
				.build();
		return apply("string-is-in", StringVal.of("match"), designator);
	}

	/** {@code integer-equal(integer-divide(1, 0), 1)}. */
	private static Expression divisionByZero()
	{
		return apply("integer-equal",
				apply("integer-divide", XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(0)),
				XacmlTypes.INTEGER.ofAny(1));
	}

	private static Expression apply(String function, Expression... arguments)
	{
		return Apply.builder(F + function, functions()::getFunction).param(arguments).build();
	}

	private static FunctionProvider functions()
	{
		return FunctionProvider.builder().withDefaultFunctions().build();
	}

	private static Result decide(Expression condition)
	{
		return decide(Rule.builder("rule", Effect.PERMIT).condition(new Condition(condition)));
	}

	private static Result decide(Rule.Builder rule)
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeDesignatorKey.class))).thenReturn(Optional.empty());
		DecisionCombiningAlgorithmProvider algorithms = DecisionCombiningAlgorithmProvider.builder()
				.withDefaultAlgorithms().build();
		DecisionCombiningAlgorithm<Rule> ruleAlgorithm = algorithms.getRuleAlgorithm(
				"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		Policy policy = Policy.builder("policy").version("1.0")
				.combiningAlgorithm(ruleAlgorithm)
				.rule(rule)
				.build();
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("later-deciding-operand")
				.policyRepository(PolicyRepository.build("later-deciding-operand-repository").build())
				.pip(pip).rootPolicy(policy).build();
		return pdp.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
	}

	private static void assertDecisionAndStatus(Result result, Decision decision, StatusCodeId status)
	{
		assertThat(result.getDecision()).isEqualTo(decision);
		assertThat(result.getStatus().getStatusCode().getValue()).isEqualTo(status);
	}
}
