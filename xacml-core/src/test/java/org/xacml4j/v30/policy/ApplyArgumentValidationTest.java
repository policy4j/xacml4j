package org.xacml4j.v30.policy;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.56: an {@code <Apply>} whose arguments do not fit
 * the function is found when the expression is built. It can still be
 * built. Its evaluation is Indeterminate with status processing-error,
 * and the function is not invoked.
 * <p>
 * XACML 3.0 core (errata 01), section 7.4: "Valid expressions SHALL be
 * type correct, which means that the types of each of the elements
 * contained within &lt;Apply&gt; elements SHALL agree with the respective
 * argument types of the function that is named by the FunctionId
 * attribute." Section 7.19.2: "If a policy that contains invalid static
 * data-types is evaluated by the XACML PDP at the time a decision request
 * is received, then the result of that policy SHALL be "Indeterminate"
 * with a StatusCode value of
 * "urn:oasis:names:tc:xacml:1.0:status:processing-error"." The
 * conformance tests IIC003 and IIC014 load such policies and expect that
 * result, so the expression is not rejected when the policy is loaded.
 * <p>
 * Before the fix the arguments were not checked. {@code integer-add(5)}
 * evaluated to 5, {@code string-equal(a, b, c)} to false, and
 * {@code integer-bag-size} counted a bag of strings.
 */
public class ApplyArgumentValidationTest
{
	private static final String F1 = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String F3 = "urn:oasis:names:tc:xacml:3.0:function:";

	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidApplies")
	public void applyWithArgumentsThatDoNotFitIsIndeterminate(String name, String functionId, Expression[] arguments)
	{
		Apply apply = Apply.builder(functionId, FUNCTIONS::getFunction).param(arguments).build();
		EvaluationContext context = mock(EvaluationContext.class);

		assertFalse(apply.isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class, () -> apply.evaluate(context));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		// The message names the function, not the arguments.
		assertEquals(Optional.of("Function=\"" + functionId + "\" is applied to arguments that do not fit it"),
				e.getStatus().getMessage());
		// The function is not invoked and no argument is evaluated.
		verifyNoInteractions(context);
	}

	static Stream<Arguments> invalidApplies()
	{
		return Stream.of(
				invalid("integer-add(5): one argument where at least two are needed", F1 + "integer-add", integer(5)),
				invalid("integer-add(): no argument", F1 + "integer-add"),
				invalid("integer-abs(): no argument", F1 + "integer-abs"),
				invalid("string-equal(a): one argument of two", F1 + "string-equal", string("a")),
				invalid("string-equal(a, b, c): three arguments for two", F1 + "string-equal",
						string("a"), string("b"), string("c")),
				invalid("integer-add(a, 1): a string where an integer is needed", F1 + "integer-add",
						string("a"), integer(1)),
				// Conformance test IIC014 has this expression.
				invalid("integer-add(1, a): a string as the second argument", F1 + "integer-add",
						integer(1), string("a")),
				invalid("string-equal(a, 1): an integer where a string is needed", F1 + "string-equal",
						string("a"), integer(1)),
				// Conformance test IIC003 has this expression, with an attribute designator as the bag.
				invalid("string-equal(a, string-bag(a)): a bag where a value is needed", F1 + "string-equal",
						string("a"), apply(F1 + "string-bag", string("a"))),
				invalid("integer-bag-size(string-bag(a)): a bag of another type", F1 + "integer-bag-size",
						apply(F1 + "string-bag", string("a"))),
				invalid("integer-one-and-only(5): a value where a bag is needed", F1 + "integer-one-and-only",
						integer(5)),
				invalid("integer-add(1, integer-bag(2)): a bag where a value is needed", F1 + "integer-add",
						integer(1), apply(F1 + "integer-bag", integer(2))),
				invalid("integer-bag(1, a): a string in a bag of integers", F1 + "integer-bag",
						integer(1), string("a")),
				invalid("any-of(string-equal, a): the bag is missing", F3 + "any-of",
						function(F1 + "string-equal"), string("a")),
				invalid("n-of(): the number is missing", F1 + "n-of"),
				invalid("not(): no argument", F1 + "not"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("validApplies")
	public void applyWithArgumentsThatFitIsEvaluated(String name, String functionId, Expression[] arguments,
			Object expected)
	{
		Apply apply = Apply.builder(functionId, FUNCTIONS::getFunction).param(arguments).build();

		assertTrue(apply.isTypeCorrect());
		assertEquals(expected, apply.evaluate(mock(EvaluationContext.class)));
	}

	static Stream<Arguments> validApplies()
	{
		return Stream.of(
				valid("integer-add(1, 2)", F1 + "integer-add", XacmlTypes.INTEGER.ofAny(3), integer(1), integer(2)),
				valid("integer-add(1, 2, 3)", F1 + "integer-add", XacmlTypes.INTEGER.ofAny(6),
						integer(1), integer(2), integer(3)),
				valid("string-equal(a, a)", F1 + "string-equal", XacmlTypes.BOOLEAN.ofAny(true),
						string("a"), string("a")),
				valid("integer-one-and-only(integer-bag(7))", F1 + "integer-one-and-only", XacmlTypes.INTEGER.ofAny(7),
						apply(F1 + "integer-bag", integer(7))),
				valid("integer-bag-size(integer-bag())", F1 + "integer-bag-size", XacmlTypes.INTEGER.ofAny(0),
						apply(F1 + "integer-bag")),
				valid("integer-bag-size(integer-bag(1, 2))", F1 + "integer-bag-size", XacmlTypes.INTEGER.ofAny(2),
						apply(F1 + "integer-bag", integer(1), integer(2))),
				valid("integer-add(1, integer-one-and-only(integer-bag(2)))", F1 + "integer-add",
						XacmlTypes.INTEGER.ofAny(3), integer(1),
						apply(F1 + "integer-one-and-only", apply(F1 + "integer-bag", integer(2)))),
				// A call without arguments is right where the function allows it (ruling D11 and run 076).
				valid("or()", F1 + "or", XacmlTypes.BOOLEAN.ofAny(false)),
				valid("and()", F1 + "and", XacmlTypes.BOOLEAN.ofAny(true)),
				valid("n-of(0)", F1 + "n-of", XacmlTypes.BOOLEAN.ofAny(true), integer(0)),
				valid("n-of(1, true, false)", F1 + "n-of", XacmlTypes.BOOLEAN.ofAny(true),
						integer(1), bool(true), bool(false)),
				valid("not(false)", F1 + "not", XacmlTypes.BOOLEAN.ofAny(true), bool(false)),
				valid("any-of(string-equal, a, string-bag(b, a))", F3 + "any-of", XacmlTypes.BOOLEAN.ofAny(true),
						function(F1 + "string-equal"), string("a"), apply(F1 + "string-bag", string("b"), string("a"))));
	}

	/**
	 * An expression that holds an expression that is not type correct is
	 * not a valid expression either (section 7.4: expressions compose
	 * greater expressions, and a valid expression is type correct). Its
	 * own arguments can fit by their declared types; it is Indeterminate
	 * all the same, and nothing is evaluated. The message names the
	 * function of the inner expression.
	 */
	@Test
	public void expressionThatHoldsATypeErrorHasATypeError()
	{
		assertHoldsTypeError((Apply) apply(F1 + "integer-equal",
				apply(F1 + "integer-add", integer(5)), integer(5)), F1 + "integer-add");
		// Three levels.
		assertHoldsTypeError((Apply) apply(F1 + "not", apply(F1 + "integer-equal",
				apply(F1 + "integer-add", integer(5)), integer(5))), F1 + "integer-add");
	}

	/**
	 * A function that evaluates its own arguments would skip the argument
	 * after the deciding one. A type error in the skipped argument is
	 * known when the expression is built, so the expression is
	 * Indeterminate and not true. Ruling D11 (a deciding operand wins over
	 * a failed one) is about failures of the evaluation.
	 */
	@Test
	public void typeErrorBehindADecidingOperandIsNotSkipped()
	{
		Expression wrong = apply(F1 + "string-equal", string("a"), apply(F1 + "string-bag", string("a")));

		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), wrong), F1 + "string-equal");
		assertHoldsTypeError((Apply) apply(F1 + "and", bool(false), wrong), F1 + "string-equal");
		assertHoldsTypeError((Apply) apply(F1 + "n-of", integer(0), wrong), F1 + "string-equal");
		assertHoldsTypeError((Apply) apply(F1 + "and", bool(true),
				apply(F1 + "or", bool(true), wrong)), F1 + "string-equal");
	}

	/** The same through a variable: the reference stands for the expression of its definition. */
	@Test
	public void typeErrorInAVariableDefinitionIsFoundThroughTheReference()
	{
		Expression wrong = apply(F1 + "string-equal", string("a"), apply(F1 + "string-bag", string("a")));
		Expression reference = new VariableReference(new VariableDefinition("wrong", wrong));
		Expression right = new VariableReference(new VariableDefinition("right",
				apply(F1 + "string-equal", string("a"), string("a"))));

		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), reference), F1 + "string-equal");
		assertTrue(((Apply) apply(F1 + "or", bool(false), right)).isTypeCorrect());
	}

	/** A definition can be a reference to another variable. The chain is followed to its end. */
	@Test
	public void typeErrorIsFoundThroughALongChainOfVariableReferences()
	{
		for (int length : new int[] {1, 64, 65, 500}) {
			Expression wrong = apply(F1 + "string-equal", string("a"), apply(F1 + "string-bag", string("a")));
			Expression right = apply(F1 + "string-equal", string("a"), string("a"));
			for (int i = 0; i < length; i++) {
				wrong = new VariableReference(new VariableDefinition("wrong-" + i, wrong));
				right = new VariableReference(new VariableDefinition("right-" + i, right));
			}

			assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), wrong), F1 + "string-equal");
			assertTrue(((Apply) apply(F1 + "or", bool(true), right)).isTypeCorrect(), "length=" + length);
		}
	}

	/** An argument of unknown type next to another violation: a type error all the same. */
	@Test
	public void knownViolationNextToAnArgumentOfUnknownTypeIsATypeError()
	{
		// A string where or takes booleans. Before the fix the or was true.
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), string("a"), stringBagOfUnknownType()), F1 + "or");
		// One argument too many.
		assertHoldsTypeError((Apply) apply(F1 + "string-bag-size", stringBagOfUnknownType(), integer(99)),
				F1 + "string-bag-size");
		// An argument is missing.
		assertHoldsTypeError((Apply) apply(F1 + "string-is-in", stringBagOfUnknownType()), F1 + "string-is-in");
		// The argument of unknown type stands where a function reference is needed.
		assertHoldsTypeError((Apply) apply(F3 + "any-of", stringBagOfUnknownType(), string("a"),
				apply(F1 + "string-bag", string("a"))), F3 + "any-of");
	}

	/**
	 * A higher-order function with a bag of unknown type, behind the
	 * deciding operand of an or. The integer can not be an argument of
	 * string-equal whatever the bag holds. Both expressions are type
	 * errors, and the or is not true.
	 */
	@Test
	public void higherOrderFunctionWithABagOfUnknownTypeIsATypeError()
	{
		Apply anyOf = (Apply) apply(F3 + "any-of", function(F1 + "string-equal"), integer(1),
				stringBagOfUnknownType());

		assertHoldsTypeError(anyOf, F3 + "any-of");
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), anyOf), F3 + "any-of");
		// With a bag of a known type the function's own validation finds the integer.
		assertHoldsTypeError((Apply) apply(F3 + "any-of", function(F1 + "string-equal"), integer(1),
				apply(F1 + "string-bag", string("a"))), F3 + "any-of");
	}

	private static void assertHoldsTypeError(Apply apply, String functionOfTheWrongExpression)
	{
		EvaluationContext context = mock(EvaluationContext.class);

		assertFalse(apply.isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class, () -> apply.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of("Function=\"" + functionOfTheWrongExpression
				+ "\" is applied to arguments that do not fit it"), e.getStatus().getMessage());
		verifyNoInteractions(context);
	}

	/** An unknown function is rejected when the expression is built, as before. */
	@Test
	public void unknownFunctionIsRejected()
	{
		assertThrows(PolicySyntaxException.class,
				() -> Apply.builder("urn:test:no-such-function", FUNCTIONS::getFunction).build());
	}

	/**
	 * The functions {@code attribute-designator} and
	 * {@code attribute-selector} return a bag of the data type that their
	 * third argument names. Before the fix their declared type was the
	 * data type itself, so every use of them as a bag looked wrong.
	 */
	@Test
	public void attributeDesignatorFunctionsHaveTheTypeOfABag()
	{
		Expression designator = apply(F3 + "attribute-designator", uri(CategoryId.RESOURCE.getId()),
				uri("urn:test:attribute"), uri(XacmlTypes.STRING.getTypeId()));
		Expression selector = apply(F3 + "attribute-selector", uri(CategoryId.RESOURCE.getId()),
				XacmlTypes.XPATH.ofAny("/a/text()", CategoryId.RESOURCE), uri(XacmlTypes.INTEGER.getTypeId()));

		assertEquals(XacmlTypes.STRING.bagType(), designator.getEvaluatesTo());
		assertEquals(XacmlTypes.INTEGER.bagType(), selector.getEvaluatesTo());
		assertTrue(((Apply) apply(F1 + "string-is-in", string("a"), designator)).isTypeCorrect());
		assertTrue(((Apply) apply(F1 + "integer-bag-size", selector)).isTypeCorrect());
		assertFalse(((Apply) apply(F1 + "integer-bag-size", designator)).isTypeCorrect());
		assertFalse(((Apply) apply(F1 + "string-equal", string("a"), designator)).isTypeCorrect());
	}

	/**
	 * Every expression has a type that is known when the policy is loaded.
	 * The data type of the {@code attribute-designator} function can be
	 * written as an expression; then the type of its bag can not be told,
	 * and an expression that takes the bag as an argument is not shown to
	 * be type correct. It is a type error, whatever the data type would
	 * be at evaluation.
	 */
	@Test
	public void argumentWhoseTypeIsNotKnownWhenTheExpressionIsBuiltIsATypeError()
	{
		// The designator function itself is applied to arguments that fit it.
		assertTrue(((Apply) stringBagOfUnknownType()).isTypeCorrect());

		assertHoldsTypeError((Apply) apply(F1 + "string-is-in", string("a"), stringBagOfUnknownType()),
				F1 + "string-is-in");
		assertHoldsTypeError((Apply) apply(F1 + "string-bag-size", stringBagOfUnknownType()),
				F1 + "string-bag-size");
		// Before the check this was the value 0: the size of a bag of strings.
		assertHoldsTypeError((Apply) apply(F1 + "integer-bag-size", stringBagOfUnknownType()),
				F1 + "integer-bag-size");
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), stringBagOfUnknownType()), F1 + "or");
	}

	/**
	 * Ruling D11 stays for failures of the evaluation: an operand that is
	 * type correct and would fail when it is evaluated is not evaluated
	 * behind the deciding operand.
	 */
	@Test
	public void decidingOperandStillWinsOverAnOperandThatFailsAtEvaluation()
	{
		// boolean-one-and-only of an empty bag is type correct and fails at evaluation.
		Apply fails = (Apply) apply(F1 + "boolean-one-and-only", apply(F1 + "boolean-bag"));
		assertTrue(fails.isTypeCorrect());
		assertThrows(EvaluationException.class, () -> fails.evaluate(mock(EvaluationContext.class)));

		Apply or = (Apply) apply(F1 + "or", bool(true), fails);
		assertTrue(or.isTypeCorrect());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), or.evaluate(mock(EvaluationContext.class)));
		Apply and = (Apply) apply(F1 + "and", bool(false), fails);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), and.evaluate(mock(EvaluationContext.class)));
	}

	/** The bag of an {@code attribute-designator} function whose data type, string, is an expression. */
	private static Expression stringBagOfUnknownType()
	{
		Expression dataType = apply(F1 + "anyURI-one-and-only",
				apply(F1 + "anyURI-bag", uri(XacmlTypes.STRING.getTypeId())));
		Expression designator = apply(F3 + "attribute-designator", uri(CategoryId.RESOURCE.getId()),
				uri("urn:test:attribute"), dataType);
		assertThrows(RuntimeException.class, designator::getEvaluatesTo);
		return designator;
	}

	/**
	 * The data type of the designator function names no data type. The
	 * type of the bag can not be told, so the expression that takes the
	 * bag is a type error. Before the check its evaluation invoked the
	 * designator function, which failed with status syntax-error.
	 */
	@Test
	public void argumentWithADataTypeThatDoesNotExistIsATypeError()
	{
		Expression designator = apply(F3 + "attribute-designator", uri(CategoryId.RESOURCE.getId()),
				uri("urn:test:attribute"), uri("urn:test:no-such-data-type"));

		assertHoldsTypeError((Apply) apply(F1 + "string-is-in", string("a"), designator), F1 + "string-is-in");
	}

	private static Arguments invalid(String name, String functionId, Expression... arguments)
	{
		return Arguments.of(name, functionId, arguments);
	}

	private static Arguments valid(String name, String functionId, Object expected, Expression... arguments)
	{
		return Arguments.of(name, functionId, arguments, expected);
	}

	private static Expression apply(String functionId, Expression... arguments)
	{
		return Apply.builder(functionId, FUNCTIONS::getFunction).param(arguments).build();
	}

	private static Expression function(String functionId)
	{
		return new FunctionReference(FUNCTIONS.getFunction(functionId).orElseThrow());
	}

	private static Expression integer(long value)
	{
		return XacmlTypes.INTEGER.ofAny(value);
	}

	private static Expression string(String value)
	{
		return XacmlTypes.STRING.ofAny(value);
	}

	private static Expression bool(boolean value)
	{
		return XacmlTypes.BOOLEAN.ofAny(value);
	}

	private static Expression uri(String value)
	{
		return XacmlTypes.ANYURI.ofAny(value);
	}
}
