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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.DynamicType;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

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

	/** What is known is checked when the expression is built, also next to a dynamic bag. */
	@Test
	public void knownViolationNextToADynamicBagIsATypeError()
	{
		// A string where or takes booleans.
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), string("a"), dynamicBag(XacmlTypes.BOOLEAN)),
				F1 + "or");
		// One argument too many.
		assertHoldsTypeError((Apply) apply(F1 + "string-bag-size", dynamicBag(XacmlTypes.STRING), integer(99)),
				F1 + "string-bag-size");
		// An argument is missing.
		assertHoldsTypeError((Apply) apply(F1 + "string-is-in", dynamicBag(XacmlTypes.STRING)), F1 + "string-is-in");
		// A bag stands where a function reference is needed.
		assertHoldsTypeError((Apply) apply(F3 + "any-of", dynamicBag(XacmlTypes.STRING), string("a"),
				apply(F1 + "string-bag", string("a"))), F3 + "any-of");
	}

	/**
	 * A higher-order function with a dynamic bag. An operand that can not
	 * fit the predicate is found when the expression is built. What
	 * depends on the data type of the bag is checked by the function when
	 * it has the bag.
	 */
	@Test
	public void higherOrderFunctionWithADynamicBag()
	{
		// The integer can not be an argument of string-equal whatever the bag holds.
		Apply wrong = (Apply) apply(F3 + "any-of", function(F1 + "string-equal"), integer(1),
				dynamicBag(XacmlTypes.STRING));
		assertHoldsTypeError(wrong, F3 + "any-of");
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), wrong), F3 + "any-of");
		// With a bag of a known type the function's own validation finds the integer too.
		assertHoldsTypeError((Apply) apply(F3 + "any-of", function(F1 + "string-equal"), integer(1),
				apply(F1 + "string-bag", string("a"))), F3 + "any-of");

		assertEvaluatesTo(XacmlTypes.BOOLEAN.ofAny(false), apply(F3 + "any-of", function(F1 + "string-equal"),
				string("a"), dynamicBag(XacmlTypes.STRING)));

		// The bag turns out to hold integers: the function finds it before it applies the predicate.
		Apply anyOf = (Apply) apply(F3 + "any-of", function(F1 + "string-equal"), string("a"),
				dynamicBag(XacmlTypes.INTEGER));
		assertTrue(anyOf.isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class,
				() -> anyOf.evaluate(mock(EvaluationContext.class)));
		assertEquals(StatusCodeId.SYNTAX_ERROR, e.getStatus().getStatusCode().getValue());
	}

	/**
	 * all-of-any, any-of-all and all-of-all take a predicate and two bags.
	 * They had no check of the bags against the predicate, neither when
	 * the expression is built nor when the function is invoked.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"all-of-any", "any-of-all", "all-of-all"})
	public void higherOrderFunctionWithTwoBagsChecksItsOperands(String name)
	{
		// The predicate takes two strings; the first bag holds integers.
		assertHoldsTypeError((Apply) apply(F1 + name, function(F1 + "string-equal"),
				apply(F1 + "integer-bag", integer(1)), apply(F1 + "string-bag", string("a"))), F1 + name);
		assertHoldsTypeError((Apply) apply(F1 + name, function(F1 + "string-equal"),
				apply(F1 + "string-bag", string("a")), apply(F1 + "integer-bag")), F1 + name);
		assertEvaluatesTo(XacmlTypes.BOOLEAN.ofAny(true), apply(F1 + name, function(F1 + "string-equal"),
				apply(F1 + "string-bag", string("a")), apply(F1 + "string-bag", string("a"))));

		// With a dynamic bag the check waits for the bag. The bag is empty: without the check
		// all-of-any and all-of-all are true over it.
		Apply dynamic = (Apply) apply(F1 + name, function(F1 + "string-equal"),
				dynamicBag(XacmlTypes.INTEGER), apply(F1 + "string-bag", string("a")));
		assertTrue(dynamic.isTypeCorrect());
		assertEquals(StatusCodeId.SYNTAX_ERROR, assertThrows(EvaluationException.class,
				() -> dynamic.evaluate(mock(EvaluationContext.class))).getStatus().getStatusCode().getValue());
		// Both bags empty: no value reaches the predicate or the any-of that any-of-all uses.
		Apply empty = (Apply) apply(F1 + name, function(F1 + "string-equal"),
				dynamicBag(XacmlTypes.INTEGER), apply(F1 + "string-bag"));
		assertTrue(empty.isTypeCorrect());
		assertEquals(StatusCodeId.SYNTAX_ERROR, assertThrows(EvaluationException.class,
				() -> empty.evaluate(mock(EvaluationContext.class))).getStatus().getStatusCode().getValue());
		assertEvaluatesTo(XacmlTypes.BOOLEAN.ofAny(!name.equals("any-of-all")),
				apply(F1 + name, function(F1 + "string-equal"),
						dynamicBag(XacmlTypes.STRING), apply(F1 + "string-bag", string("a"))));
	}

	private static void assertHoldsTypeError(Apply apply, String functionOfTheWrongExpression)
	{
		EvaluationContext context = mock(EvaluationContext.class);

		assertFalse(apply.isTypeCorrect());
		assertDoesNotFit(functionOfTheWrongExpression,
				assertThrows(EvaluationException.class, () -> apply.evaluate(context)));
		verifyNoInteractions(context);
	}

	/** The status of an expression whose arguments do not fit. The message names the function only. */
	private static void assertDoesNotFit(String functionId, EvaluationException e)
	{
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of("Function=\"" + functionId + "\" is applied to arguments that do not fit it"),
				e.getStatus().getMessage());
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
	 * Bead policy4j-the.4.61. The bag of an {@code attribute-designator}
	 * function whose data type is an expression has the dynamic type: a
	 * bag of a data type that is known only at evaluation (Related and
	 * Nested Entities Profile, section 6.1: "The value of the argument
	 * specifies the value of the DataType XML attribute of the XACML
	 * attribute values returned by the function."). Where a parameter
	 * takes a bag the expression is accepted, and the bag is checked
	 * against the parameter when it is evaluated, before the function is
	 * invoked.
	 */
	@Test
	public void dynamicBagIsAcceptedWhereABagFitsAndCheckedAtEvaluation()
	{
		assertEquals(DynamicType.INSTANCE.bagType(), dynamicBag(XacmlTypes.STRING).getEvaluatesTo());
		assertTrue(((Apply) dynamicBag(XacmlTypes.STRING)).isTypeCorrect());

		// The request has no such category: the designator gives an empty bag of its data type.
		assertEvaluatesTo(XacmlTypes.INTEGER.ofAny(0), apply(F1 + "string-bag-size", dynamicBag(XacmlTypes.STRING)));
		assertEvaluatesTo(XacmlTypes.BOOLEAN.ofAny(false),
				apply(F1 + "string-is-in", string("a"), dynamicBag(XacmlTypes.STRING)));
		assertEvaluatesTo(XacmlTypes.INTEGER.ofAny(0), apply(F1 + "integer-bag-size", dynamicBag(XacmlTypes.INTEGER)));

		// A bag of strings where a bag of integers is needed. Before bead 4.56 this was the value 0.
		assertFailsAtEvaluation(apply(F1 + "integer-bag-size", dynamicBag(XacmlTypes.STRING)),
				F1 + "integer-bag-size");
		assertFailsAtEvaluation(apply(F1 + "string-is-in", string("a"), dynamicBag(XacmlTypes.INTEGER)),
				F1 + "string-is-in");
	}

	/** The shape of a dynamic bag is known: it is a type error where a single value is needed. */
	@Test
	public void dynamicBagIsATypeErrorWhereAValueIsNeeded()
	{
		assertHoldsTypeError((Apply) apply(F1 + "string-equal", string("a"), dynamicBag(XacmlTypes.STRING)),
				F1 + "string-equal");
		assertHoldsTypeError((Apply) apply(F1 + "or", bool(true), dynamicBag(XacmlTypes.BOOLEAN)), F1 + "or");
		assertHoldsTypeError((Apply) apply(F1 + "integer-add", integer(1), dynamicBag(XacmlTypes.INTEGER)),
				F1 + "integer-add");
	}

	/**
	 * An operand that holds a dynamic bag, behind the deciding operand:
	 * it is not evaluated, so its value is not checked (ruling D11). It
	 * is no type error when the expression is built.
	 */
	@Test
	public void dynamicOperandBehindADecidingOperandIsNotEvaluated()
	{
		EvaluationContext context = mock(EvaluationContext.class);
		// integer-bag-size on a bag of strings fails when it is evaluated.
		Expression fails = apply(F1 + "integer-equal",
				apply(F1 + "integer-bag-size", dynamicBag(XacmlTypes.STRING)), integer(0));
		Apply or = (Apply) apply(F1 + "or", bool(true), fails);

		assertTrue(or.isTypeCorrect());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), or.evaluate(context));
		// The request was not asked for the category of the designator.
		verify(context, never()).resolveEntity(any());
		assertThrows(EvaluationException.class, () -> fails.evaluate(mock(EvaluationContext.class)));
	}

	/** A reference to a designator function has no arguments that tell its data type: a dynamic bag. */
	@Test
	public void referenceToADesignatorFunctionHasTheDynamicBagType()
	{
		assertEquals(DynamicType.INSTANCE.bagType(), function(F3 + "attribute-designator").getEvaluatesTo());
		assertEquals(DynamicType.INSTANCE.bagType(), function(F3 + "attribute-selector").getEvaluatesTo());
	}

	/**
	 * Every function that takes a function reference refuses one that
	 * returns a bag. So none is given a reference of dynamic type.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("higherOrderFunctionsWithOperands")
	public void referenceToADesignatorFunctionIsNoPredicate(String functionId, Expression[] operands)
	{
		Expression[] arguments = new Expression[operands.length + 1];
		arguments[0] = function(F3 + "attribute-designator");
		System.arraycopy(operands, 0, arguments, 1, operands.length);

		assertHoldsTypeError((Apply) apply(functionId, arguments), functionId);
	}

	static Stream<Arguments> higherOrderFunctionsWithOperands()
	{
		Expression value = uri("urn:test:a");
		Expression bag = apply(F1 + "anyURI-bag", uri("urn:test:a"));
		return Stream.of(
				Arguments.of(F3 + "any-of", new Expression[]{value, bag}),
				Arguments.of(F3 + "all-of", new Expression[]{value, bag}),
				Arguments.of(F3 + "any-of-any", new Expression[]{bag, bag}),
				Arguments.of(F1 + "all-of-any", new Expression[]{bag, bag}),
				Arguments.of(F1 + "any-of-all", new Expression[]{bag, bag}),
				Arguments.of(F1 + "all-of-all", new Expression[]{bag, bag}),
				Arguments.of(F3 + "map", new Expression[]{bag}));
	}

	/** The other higher-order functions check a dynamic bag when they have it, as any-of does. */
	@ParameterizedTest(name = "{0}")
	@MethodSource("higherOrderFunctionsWithADynamicBagOfAnotherDataType")
	public void higherOrderFunctionFindsADynamicBagOfAnotherDataType(String functionId, Expression[] arguments)
	{
		Apply apply = (Apply) apply(functionId, arguments);

		assertTrue(apply.isTypeCorrect());
		EvaluationException e = assertThrows(EvaluationException.class,
				() -> apply.evaluate(mock(EvaluationContext.class)));
		assertEquals(StatusCodeId.SYNTAX_ERROR, e.getStatus().getStatusCode().getValue());
	}

	static Stream<Arguments> higherOrderFunctionsWithADynamicBagOfAnotherDataType()
	{
		Expression equal = function(F1 + "string-equal");
		return Stream.of(
				Arguments.of(F3 + "all-of", new Expression[]{equal, string("a"), dynamicBag(XacmlTypes.INTEGER)}),
				Arguments.of(F3 + "any-of-any", new Expression[]{equal, dynamicBag(XacmlTypes.INTEGER),
						apply(F1 + "string-bag", string("a"))}),
				Arguments.of(F3 + "map", new Expression[]{function(F1 + "string-normalize-space"),
						dynamicBag(XacmlTypes.INTEGER)}));
	}

	/** A dynamic bag behind a variable reference is checked like the bag itself. */
	@Test
	public void dynamicBagBehindAVariableReferenceIsChecked()
	{
		Expression strings = new VariableReference(new VariableDefinition("strings", dynamicBag(XacmlTypes.STRING)));

		assertEquals(DynamicType.INSTANCE.bagType(), strings.getEvaluatesTo());
		assertEvaluatesTo(XacmlTypes.INTEGER.ofAny(0), apply(F1 + "string-bag-size", strings));
		assertFailsAtEvaluation(apply(F1 + "integer-bag-size", strings), F1 + "integer-bag-size");
	}

	/**
	 * The expression gives a data type that does not exist. The designator
	 * function finds that when it is evaluated.
	 */
	@Test
	public void dynamicDataTypeThatDoesNotExistIsFoundAtEvaluation()
	{
		Expression type = apply(F1 + "anyURI-one-and-only",
				apply(F1 + "anyURI-bag", uri("urn:test:no-such-data-type")));
		Apply size = (Apply) apply(F1 + "string-bag-size", apply(F3 + "attribute-designator",
				uri(CategoryId.RESOURCE.getId()), uri("urn:test:attribute"), type));

		assertTrue(size.isTypeCorrect());
		assertThrows(EvaluationException.class, () -> size.evaluate(mock(EvaluationContext.class)));
	}

	/**
	 * An argument beyond the last parameter belongs to the variadic
	 * parameter, and a dynamic one is checked against it. The function is
	 * not invoked with a bag that does not fit.
	 */
	@Test
	public void dynamicArgumentOfAVariadicParameterIsChecked() throws Exception
	{
		FunctionInvocation invocation = mock(FunctionInvocation.class);
		FunctionSpec bags = FunctionSpecBuilder.builder("urn:test:function:string-bags")
				.varArg(XacmlTypes.STRING.bagType(), 1, 10)
				.build(XacmlTypes.BOOLEAN, invocation);
		when(invocation.invoke(eq(bags), any(), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		Expression strings = apply(F1 + "string-bag", string("a"));

		Apply fits = Apply.builder(bags)
				.param(strings, dynamicBag(XacmlTypes.STRING), dynamicBag(XacmlTypes.STRING)).build();
		assertTrue(fits.isTypeCorrect());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), fits.evaluate(mock(EvaluationContext.class)));
		verify(invocation, times(1)).invoke(eq(bags), any(), anyList());

		Apply wrong = Apply.builder(bags)
				.param(strings, dynamicBag(XacmlTypes.STRING), dynamicBag(XacmlTypes.INTEGER)).build();
		assertFailsAtEvaluation(wrong, "urn:test:function:string-bags");
		// Still the one invocation of the expression that fits.
		verify(invocation, times(1)).invoke(eq(bags), any(), anyList());

		// A bag of a known other data type is found when the expression is built.
		assertFalse(Apply.builder(bags).param(strings, apply(F1 + "integer-bag", integer(1))).build()
				.isTypeCorrect());
	}

	/**
	 * An extension function can return a single value of dynamic type. A
	 * function that evaluates its own arguments checks such an operand
	 * when it evaluates it, and not when it does not need it.
	 */
	@Test
	public void dynamicValueIsCheckedWhenItIsEvaluated() throws Exception
	{
		FunctionInvocation invocation = mock(FunctionInvocation.class);
		FunctionSpec value = FunctionSpecBuilder.builder("urn:test:function:dynamic-value")
				.build(DynamicType.INSTANCE, invocation);
		Expression dynamic = Apply.builder(value).build();
		assertEquals(DynamicType.INSTANCE, dynamic.getEvaluatesTo());

		// A bag is needed: the shape is known when the expression is built.
		assertHoldsTypeError((Apply) apply(F1 + "string-bag-size", dynamic), F1 + "string-bag-size");

		// The deciding operand comes first: the dynamic one is not evaluated.
		assertEvaluatesTo(bool(true), apply(F1 + "or", bool(true), dynamic));
		verifyNoInteractions(invocation);

		when(invocation.invoke(eq(value), any(), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		assertEvaluatesTo(bool(true), apply(F1 + "or", bool(false), dynamic));
		assertFailsAtEvaluation(apply(F1 + "string-equal", string("a"), dynamic), F1 + "string-equal");

		when(invocation.invoke(eq(value), any(), anyList())).thenReturn(XacmlTypes.STRING.ofAny("a"));
		assertFailsAtEvaluation(apply(F1 + "or", bool(false), dynamic), F1 + "or");
		assertEvaluatesTo(bool(true), apply(F1 + "string-equal", string("a"), dynamic));
	}

	/** A function without parameters takes no argument, a dynamic one included. */
	@Test
	public void functionWithoutParametersTakesNoArguments()
	{
		FunctionInvocation invocation = mock(FunctionInvocation.class);
		String id = "urn:test:function:no-parameters";
		FunctionSpec none = FunctionSpecBuilder.builder(id).build(XacmlTypes.BOOLEAN, invocation);

		assertTrue(Apply.builder(none).build().isTypeCorrect());
		assertHoldsTypeError(Apply.builder(none).param(string("a")).build(), id);
		assertHoldsTypeError(Apply.builder(none).param(dynamicBag(XacmlTypes.STRING)).build(), id);
		// The check without an exception, which a context can ask for at every invocation.
		assertTrue(none.validateParameters(ImmutableList.of()));
		assertFalse(none.validateParameters(ImmutableList.of(string("a"))));
		verifyNoInteractions(invocation);
	}

	/** The check that a context can ask for at every invocation sees the checked arguments and accepts them. */
	@Test
	public void dynamicArgumentPassesTheValidationAtInvocation()
	{
		EvaluationContext context = mock(EvaluationContext.class);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);

		Object size = apply(F1 + "string-bag-size", dynamicBag(XacmlTypes.STRING)).evaluate(context);
		Object anyOf = apply(F3 + "any-of", function(F1 + "string-equal"),
				string("a"), dynamicBag(XacmlTypes.STRING)).evaluate(context);

		assertEquals(integer(0), size);
		assertEquals(bool(false), anyOf);
		assertThrows(EvaluationException.class,
				() -> apply(F1 + "integer-bag-size", dynamicBag(XacmlTypes.STRING)).evaluate(context));
	}

	private static void assertEvaluatesTo(Object expected, Expression expression)
	{
		assertTrue(((Apply) expression).isTypeCorrect());
		assertEquals(expected, expression.evaluate(mock(EvaluationContext.class)));
	}

	/** Type correct when the expression is built; the value does not fit when it arrives. */
	private static void assertFailsAtEvaluation(Expression expression, String functionId)
	{
		assertTrue(((Apply) expression).isTypeCorrect());
		assertDoesNotFit(functionId, assertThrows(EvaluationException.class,
				() -> expression.evaluate(mock(EvaluationContext.class))));
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

	/**
	 * The bag of an {@code attribute-designator} function whose data type
	 * is an expression. At evaluation the expression gives {@code dataType}.
	 */
	private static Expression dynamicBag(XacmlTypes dataType)
	{
		Expression type = apply(F1 + "anyURI-one-and-only",
				apply(F1 + "anyURI-bag", uri(dataType.getTypeId())));
		return apply(F3 + "attribute-designator", uri(CategoryId.RESOURCE.getId()),
				uri("urn:test:attribute"), type);
	}

	/**
	 * The data type of the designator function is a literal that names no
	 * data type. That is known when the expression is built: the type of
	 * the bag can not be told, and the expression that takes the bag is a
	 * type error.
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
