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
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.56, through the PDP: a rule whose condition holds
 * an expression that is not type correct is Indeterminate with status
 * processing-error (XACML 3.0 core, section 7.19.2). Before the fix each
 * of these conditions was true, and the rule gave its effect.
 */
public class InvalidApplyDecisionTest
{
	private static final String F1 = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String F3 = "urn:oasis:names:tc:xacml:3.0:function:";

	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	@ParameterizedTest(name = "{0}")
	@MethodSource("conditionsThatAreNotTypeCorrect")
	public void permitRuleWithAnInvalidExpressionIsIndeterminate(String name, Expression condition)
	{
		Result result = decide(condition, Effect.PERMIT);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("conditionsThatAreNotTypeCorrect")
	public void denyRuleWithAnInvalidExpressionIsIndeterminate(String name, Expression condition)
	{
		Result result = decide(condition, Effect.DENY);

		assertEquals(Decision.INDETERMINATE_D, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
	}

	static Stream<Arguments> conditionsThatAreNotTypeCorrect()
	{
		return Stream.of(
				Arguments.of("integer-equal(integer-add(5), 5): too few arguments",
						apply("integer-equal", apply("integer-add", integer(5)), integer(5))),
				Arguments.of("not(string-equal(a, b, c)): too many arguments",
						apply("not", apply("string-equal", string("a"), string("b"), string("c")))),
				Arguments.of("integer-equal(integer-bag-size(string-bag(a, b)), 2): a bag of another type",
						apply("integer-equal", apply("integer-bag-size",
								apply("string-bag", string("a"), string("b"))), integer(2))),
				// The wrong expression stands behind the operand that decides the or.
				Arguments.of("or(true, string-equal(a, string-bag(a))): a type error in a skipped branch",
						apply("or", XacmlTypes.BOOLEAN.ofAny(true),
								apply("string-equal", string("a"), apply("string-bag", string("a"))))),
				Arguments.of("or(true, variable): the type error is 65 variable references away",
						apply("or", XacmlTypes.BOOLEAN.ofAny(true), references(65,
								apply("string-equal", string("a"), apply("string-bag", string("a")))))),
				// The integer can not be an argument of string-equal, whatever the dynamic bag holds.
				Arguments.of("or(true, any-of(string-equal, 1, dynamic bag)): a wrong operand next to a dynamic bag",
						apply("or", XacmlTypes.BOOLEAN.ofAny(true), applyById(F3 + "any-of",
								new FunctionReference(FUNCTIONS.getFunction(F1 + "string-equal").orElseThrow()),
								integer(1), dynamicBagOfStrings()))),
				// Type correct when the policy is loaded; the bag holds strings when it is evaluated.
				Arguments.of("integer-equal(integer-bag-size(dynamic bag of strings), 0): found at evaluation",
						apply("integer-equal", apply("integer-bag-size", dynamicBagOfStrings()), integer(0))));
	}

	/**
	 * Bead policy4j-the.4.61: the bag of a designator function whose data
	 * type is an expression can be used where its data type fits.
	 */
	@Test
	public void ruleWithADynamicBagOfTheRightDataTypeGivesItsEffect()
	{
		Result result = decide(apply("integer-equal", apply("string-bag-size", dynamicBagOfStrings()), integer(0)),
				Effect.PERMIT);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
	}

	/**
	 * The bag of an attribute-designator function whose data type is an
	 * expression that gives the string type. The request has no such
	 * category, so the bag is empty.
	 */
	private static Expression dynamicBagOfStrings()
	{
		Expression dataType = apply("anyURI-one-and-only",
				apply("anyURI-bag", XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId())));
		return applyById(F3 + "attribute-designator",
				XacmlTypes.ANYURI.ofAny("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"),
				XacmlTypes.ANYURI.ofAny("urn:test:attribute"), dataType);
	}

	private static Expression applyById(String functionId, Expression... arguments)
	{
		return Apply.builder(functionId, FUNCTIONS::getFunction).param(arguments).build();
	}

	/** A chain of variable references that ends in the given expression. */
	private static Expression references(int length, Expression expression)
	{
		Expression e = expression;
		for (int i = 0; i < length; i++) {
			e = new VariableReference(new VariableDefinition("variable-" + i, e));
		}
		return e;
	}

	@Test
	public void ruleWithATypeCorrectExpressionGivesItsEffect()
	{
		Result result = decide(apply("integer-equal", apply("integer-add", integer(2), integer(3)), integer(5)),
				Effect.PERMIT);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
	}

	private static Result decide(Expression condition, Effect effect)
	{
		DecisionCombiningAlgorithm<Rule> firstApplicable = DecisionCombiningAlgorithmProvider.builder()
				.withDefaultAlgorithms().build()
				.getRuleAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		Policy policy = Policy.builder("policy").version("1.0").combiningAlgorithm(firstApplicable)
				.rule(Rule.builder("rule", effect).condition(new Condition(condition))).build();
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("invalid-apply")
				.policyRepository(PolicyRepository.build("invalid-apply-repository").build())
				.pip(mock(PolicyInformationPoint.class)).rootPolicy(policy).build();
		return pdp.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
	}

	private static Expression apply(String function, Expression... arguments)
	{
		return Apply.builder(F1 + function, FUNCTIONS::getFunction).param(arguments).build();
	}

	private static Expression integer(long value)
	{
		return XacmlTypes.INTEGER.ofAny(value);
	}

	private static Expression string(String value)
	{
		return XacmlTypes.STRING.ofAny(value);
	}
}
