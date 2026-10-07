package org.xacml4j.v30.policy.function.impl;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Maintainer ruling 2026-10-07 (run 061 D11, bead policy4j-the.4.12): in
 * {@code or}, {@code and}, {@code n-of} and the higher-order bag functions a
 * later deciding value wins over an earlier error.
 * <p>
 * XACML 3.0 Plus Errata 01, A.3.5: {@code or} "SHALL return True if at least
 * one of its arguments evaluates to True"; {@code and} "SHALL return False if
 * one of its arguments evaluates to False"; {@code n-of} "SHALL stop and
 * return True if the specified number of arguments evaluate to True" and
 * "SHALL stop if it is determined that evaluating the remaining arguments
 * will not satisfy the requirement". A.3.12 combines the applications of the
 * predicate with {@code or} ({@code any-of}, {@code any-of-any}) and
 * {@code and} ({@code all-of}, {@code all-of-all}); {@code all-of-any} and
 * {@code any-of-all} combine {@code any-of} results with {@code and}.
 */
public class LaterDecidingOperandTest
{
	private static final String F = "urn:oasis:names:tc:xacml:";
	private static final String PREDICATE = "urn:test:function:integer-greater-than-or-fail";
	private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder()
			.withDefaultFunctions()
			.fromClass(FailingPredicates.class)
			.build();

	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		this.context = mock(EvaluationContext.class);
		FailingPredicates.calls = 0;
	}

	/**
	 * Operands: {@code T} true, {@code F} false, {@code E} fails with
	 * processing-error, {@code M} fails with missing-attribute. Expected:
	 * {@code T}, {@code F}, or the status of the Indeterminate ({@code E} or
	 * {@code M}), which is the status of the first operand that failed.
	 * {@code evaluated} is the number of operands that are evaluated, from
	 * the first one; -1 does not check it.
	 */
	@ParameterizedTest(name = "{0} n={1} operands={2} gives {3}")
	@MethodSource("logicalRows")
	public void logicalFunctionLetsALaterDecidingOperandWin(
			String function, int n, String operands, char expected, int evaluated)
	{
		List<Operand> values = new ArrayList<>();
		List<Expression> arguments = new ArrayList<>();
		if (n >= 0) {
			arguments.add(XacmlTypes.INTEGER.ofAny(n));
		}
		for (char kind : operands.toCharArray()) {
			Operand operand = new Operand(kind);
			values.add(operand);
			arguments.add(operand);
		}
		FunctionSpec spec = require(F + "1.0:function:" + function);

		assertOutcome(expected, () -> spec.invoke(context, arguments.toArray(new Expression[0])));

		if (evaluated >= 0) {
			StringBuilder actual = new StringBuilder();
			StringBuilder wanted = new StringBuilder();
			for (int i = 0; i < values.size(); i++) {
				actual.append(values.get(i).evaluations);
				wanted.append(i < evaluated ? 1 : 0);
			}
			assertEquals(wanted.toString(), actual.toString(), "evaluations per operand");
		}
	}

	static Stream<Arguments> logicalRows()
	{
		return Stream.of(
				// or: True wins over an error; without a True an error is Indeterminate.
				Arguments.of("or", -1, "", 'F', 0),
				Arguments.of("or", -1, "FF", 'F', 2),
				Arguments.of("or", -1, "TE", 'T', 1),
				Arguments.of("or", -1, "ET", 'T', 2),
				Arguments.of("or", -1, "EFT", 'T', 3),
				Arguments.of("or", -1, "EF", 'E', 2),
				Arguments.of("or", -1, "FE", 'E', 2),
				Arguments.of("or", -1, "ME", 'M', 2),
				// and: False wins over an error; without a False an error is Indeterminate.
				Arguments.of("and", -1, "", 'T', 0),
				Arguments.of("and", -1, "TT", 'T', 2),
				Arguments.of("and", -1, "FE", 'F', 1),
				Arguments.of("and", -1, "EF", 'F', 2),
				Arguments.of("and", -1, "ETF", 'F', 3),
				Arguments.of("and", -1, "ET", 'E', 2),
				Arguments.of("and", -1, "TE", 'E', 2),
				Arguments.of("and", -1, "ME", 'M', 2),
				// n-of: n True values win; it is False when n cannot be reached even if every
				// failed operand were True; otherwise an error is Indeterminate.
				Arguments.of("n-of", 0, "", 'T', 0),
				Arguments.of("n-of", 0, "E", 'T', 0),
				Arguments.of("n-of", 1, "FF", 'F', 2),
				Arguments.of("n-of", 1, "TE", 'T', 1),
				Arguments.of("n-of", 1, "ET", 'T', 2),
				Arguments.of("n-of", 2, "TTE", 'T', 2),
				Arguments.of("n-of", 2, "TFT", 'T', 3),
				Arguments.of("n-of", 2, "ETT", 'T', 3),
				Arguments.of("n-of", 2, "FFE", 'F', 2),
				Arguments.of("n-of", 2, "EFF", 'F', 3),
				// rv082: 3 cannot be reached once one operand is false, whatever the failed one was.
				Arguments.of("n-of", 3, "EFF", 'F', 2),
				Arguments.of("n-of", 3, "EFT", 'F', 2),
				Arguments.of("n-of", 3, "ETT", 'E', 3),
				Arguments.of("n-of", 2, "ETF", 'E', 3),
				Arguments.of("n-of", 1, "EF", 'E', 2),
				Arguments.of("n-of", 2, "ME", 'M', 2),
				// Fewer operands than n is Indeterminate (A.3.5).
				Arguments.of("n-of", 3, "TT", 'E', -1));
	}

	/**
	 * The predicate is {@code a > b}; it fails with missing-attribute when an
	 * argument is -2 and with processing-error when an argument is another
	 * negative number. Expected: {@code T}, {@code F}, or the status of the
	 * Indeterminate ({@code E} or {@code M}).
	 */
	@ParameterizedTest(name = "{0}({1}, {2}) gives {3}")
	@MethodSource("higherOrderRows")
	public void higherOrderFunctionLetsALaterDecidingElementWin(
			String function, Expression first, Expression second, char expected)
	{
		FunctionSpec spec = require(function);
		FunctionReference predicate = new FunctionReference(require(PREDICATE));

		assertOutcome(expected, () -> spec.invoke(context, predicate, first, second));
	}

	static Stream<Arguments> higherOrderRows()
	{
		String anyOf = F + "3.0:function:any-of";
		String allOf = F + "3.0:function:all-of";
		String anyOfAny = F + "3.0:function:any-of-any";
		String allOfAny = F + "1.0:function:all-of-any";
		String anyOfAll = F + "1.0:function:any-of-all";
		String allOfAll = F + "1.0:function:all-of-all";
		return Stream.of(
				// any-of(p, 5, bag): results combined with or.
				Arguments.of(anyOf, integer(5), ints(3, -1), 'T'),
				Arguments.of(anyOf, integer(5), ints(-1, 3), 'T'),
				Arguments.of(anyOf, integer(5), ints(-1, 9), 'E'),
				Arguments.of(anyOf, integer(5), ints(9, -1), 'E'),
				Arguments.of(anyOf, integer(5), ints(9, 8), 'F'),
				// rv081 round 3: without a deciding value the status is the one of the first failure.
				Arguments.of(anyOf, integer(5), ints(-2, -1), 'M'),
				Arguments.of(anyOf, integer(5), ints(-1, -2), 'E'),
				Arguments.of(allOf, integer(5), ints(-2, -1), 'M'),
				Arguments.of(allOfAll, ints(-2, -1), ints(3), 'M'),
				// all-of(p, 5, bag): results combined with and.
				Arguments.of(allOf, integer(5), ints(9, -1), 'F'),
				Arguments.of(allOf, integer(5), ints(-1, 9), 'F'),
				Arguments.of(allOf, integer(5), ints(-1, 3), 'E'),
				Arguments.of(allOf, integer(5), ints(3, -1), 'E'),
				Arguments.of(allOf, integer(5), ints(3, 4), 'T'),
				// any-of-any(p, A, B): every tuple, combined with or.
				Arguments.of(anyOfAny, ints(-1, 5), ints(3), 'T'),
				Arguments.of(anyOfAny, ints(5), ints(-1, 3), 'T'),
				Arguments.of(anyOfAny, ints(-1, 1), ints(3), 'E'),
				Arguments.of(anyOfAny, ints(1), ints(3), 'F'),
				// all-of-any(p, A, B): any-of(p, a, B) for each a of A, combined with and.
				Arguments.of(allOfAny, ints(5), ints(-1, 3), 'T'),
				Arguments.of(allOfAny, ints(-1, 1), ints(3), 'F'),
				Arguments.of(allOfAny, ints(-1, 5), ints(3), 'E'),
				Arguments.of(allOfAny, ints(5, 6), ints(3), 'T'),
				// any-of-all(p, A, B): any-of(p, A, b) for each b of B, combined with and.
				Arguments.of(anyOfAll, ints(-1, 5), ints(3), 'T'),
				Arguments.of(anyOfAll, ints(5), ints(-1, 9), 'F'),
				Arguments.of(anyOfAll, ints(5), ints(-1, 3), 'E'),
				Arguments.of(anyOfAll, ints(5, 6), ints(3, 4), 'T'),
				// all-of-all(p, A, B): every pair, combined with and.
				Arguments.of(allOfAll, ints(-1, 1), ints(3), 'F'),
				Arguments.of(allOfAll, ints(5), ints(-1, 9), 'F'),
				Arguments.of(allOfAll, ints(-1, 5), ints(3), 'E'),
				Arguments.of(allOfAll, ints(5, 6), ints(3, 4), 'T'));
	}

	/**
	 * rv081 round 2: the integer of {@code n-of} is evaluated first. When it
	 * fails, the result is Indeterminate and no other argument is evaluated.
	 */
	@Test
	public void nOfWithAFailedIntegerEvaluatesNoOperand()
	{
		Expression failingInteger = new Expression() {
			@Override
			public ValueExpTypeInfo getEvaluatesTo() {
				return XacmlTypes.INTEGER;
			}

			@Override
			public <T extends Expression> T evaluate(EvaluationContext context) {
				throw new EvaluationException(Status.processingError().build(), "integer failed");
			}

			@Override
			public void accept(ExpressionVisitor visitor) {
			}
		};
		Operand first = new Operand('T');
		Operand second = new Operand('T');

		assertOutcome('E', () -> require(F + "1.0:function:n-of").invoke(context, failingInteger, first, second));

		assertEquals(0, first.evaluations + second.evaluations);
	}

	/**
	 * rv081 round 2: the higher-order functions stop applying the predicate
	 * at the element that decides the result.
	 */
	@Test
	public void higherOrderFunctionStopsAtTheDecidingElement()
	{
		FunctionReference predicate = new FunctionReference(require(PREDICATE));

		assertEquals(BooleanVal.of(true),
				require(F + "3.0:function:any-of").invoke(context, predicate, integer(5), ints(3, 9, 8)));
		assertEquals(1, FailingPredicates.calls, "any-of stops at the first true application");

		FailingPredicates.calls = 0;
		assertEquals(BooleanVal.of(false),
				require(F + "3.0:function:all-of").invoke(context, predicate, integer(5), ints(9, 3, 4)));
		assertEquals(1, FailingPredicates.calls, "all-of stops at the first false application");

		FailingPredicates.calls = 0;
		assertEquals(BooleanVal.of(true),
				require(F + "3.0:function:any-of").invoke(context, predicate, integer(5), ints(-1, 3, 9)));
		assertEquals(2, FailingPredicates.calls, "any-of goes on after a failed application and stops at the true one");

		FailingPredicates.calls = 0;
		assertEquals(BooleanVal.of(false),
				require(F + "1.0:function:all-of-all").invoke(context, predicate, ints(5, 6), ints(9, 1)));
		assertEquals(1, FailingPredicates.calls, "all-of-all stops at the first false pair");
	}

	/**
	 * The examples of A.3.12, each of which "SHALL evaluate to True". The
	 * predicate integer-greater-than is not commutative, so the examples of
	 * {@code all-of-any} and {@code any-of-all} also fix the order of the
	 * predicate arguments: the value of the first bag is the first argument.
	 * A.3.12 on {@code any-of-all}: "for all of the values in the second bag,
	 * there is a value in the first bag that is greater" (rv082).
	 */
	@Test
	public void examplesOfTheSpecificationEvaluateToTrue()
	{
		FunctionReference stringEqual = new FunctionReference(require(F + "1.0:function:string-equal"));
		FunctionReference greaterThan = new FunctionReference(require(F + "1.0:function:integer-greater-than"));
		BooleanVal yes = BooleanVal.of(true);

		assertEquals(yes, require(F + "3.0:function:any-of").invoke(context, stringEqual,
				XacmlTypes.STRING.ofAny("Paul"), strings("John", "Paul", "George", "Ringo")));
		assertEquals(yes, require(F + "3.0:function:all-of").invoke(context, greaterThan,
				integer(10), ints(9, 3, 4, 2)));
		assertEquals(yes, require(F + "3.0:function:any-of-any").invoke(context, stringEqual,
				strings("Ringo", "Mary"), strings("John", "Paul", "George", "Ringo")));
		assertEquals(yes, require(F + "1.0:function:all-of-any").invoke(context, greaterThan,
				ints(10, 20), ints(1, 3, 5, 19)));
		assertEquals(yes, require(F + "1.0:function:any-of-all").invoke(context, greaterThan,
				ints(3, 5), ints(1, 2, 3, 4)));
		assertEquals(yes, require(F + "1.0:function:all-of-all").invoke(context, greaterThan,
				ints(6, 5), ints(1, 2, 3, 4)));

		// The two mixed examples with the bags exchanged are false: 1 is not greater than any
		// value of (10, 20); no value of (1, 2, 3, 4) is greater than 5.
		BooleanVal no = BooleanVal.of(false);
		assertEquals(no, require(F + "1.0:function:all-of-any").invoke(context, greaterThan,
				ints(1, 3, 5, 19), ints(10, 20)));
		assertEquals(no, require(F + "1.0:function:any-of-all").invoke(context, greaterThan,
				ints(1, 2, 3, 4), ints(3, 5)));
	}

	/**
	 * A.3.12 quantifier order (rv081 round 1): {@code all-of-any} is "for each
	 * value of the first bag there is a value of the second bag", and
	 * {@code any-of-all} is "for each value of the second bag there is a value
	 * of the first bag". With integer-equal and both bags (1, 2) both are true;
	 * "there is a value for which every value matches" would be false.
	 */
	@Test
	public void mixedQuantifiersAreForEachThenThereIs()
	{
		FunctionReference integerEqual = new FunctionReference(require(F + "1.0:function:integer-equal"));

		assertEquals(BooleanVal.of(true),
				require(F + "1.0:function:all-of-any").invoke(context, integerEqual, ints(1, 2), ints(1, 2)));
		assertEquals(BooleanVal.of(true),
				require(F + "1.0:function:any-of-all").invoke(context, integerEqual, ints(1, 2), ints(1, 2)));
		// all-of-any((1, 2), (1)): 2 has no equal value. any-of-all((1), (1, 2)): 2 has no equal value.
		assertEquals(BooleanVal.of(false),
				require(F + "1.0:function:all-of-any").invoke(context, integerEqual, ints(1, 2), ints(1)));
		assertEquals(BooleanVal.of(false),
				require(F + "1.0:function:any-of-all").invoke(context, integerEqual, ints(1), ints(1, 2)));
		// The other direction is true: every value has an equal value in the larger bag.
		assertEquals(BooleanVal.of(true),
				require(F + "1.0:function:all-of-any").invoke(context, integerEqual, ints(1), ints(1, 2)));
		assertEquals(BooleanVal.of(true),
				require(F + "1.0:function:any-of-all").invoke(context, integerEqual, ints(1, 2), ints(1)));
	}

	private static void assertOutcome(char expected, java.util.function.Supplier<ValueExp> call)
	{
		if (expected == 'T' || expected == 'F') {
			assertEquals(BooleanVal.of(expected == 'T'), call.get());
			return;
		}
		EvaluationException error = assertThrows(EvaluationException.class, call::get);
		assertEquals(expected == 'M' ? StatusCodeId.MISSING_ATTRIBUTE : StatusCodeId.STATUS_PROCESSING_ERROR,
				error.getStatus().getStatusCode().getValue());
	}

	private static FunctionSpec require(String functionId)
	{
		return FUNCTIONS.getFunction(functionId)
				.orElseThrow(() -> new AssertionError("missing function " + functionId));
	}

	private static IntegerVal integer(int value)
	{
		return XacmlTypes.INTEGER.ofAny(value);
	}

	private static BagOfValues strings(String... values)
	{
		BagOfValues.Builder builder = XacmlTypes.STRING.bagBuilder();
		for (String value : values) {
			builder.value(value);
		}
		return builder.build();
	}

	private static BagOfValues ints(int... values)
	{
		BagOfValues.Builder builder = XacmlTypes.INTEGER.bagBuilder();
		for (int value : values) {
			builder.value(value);
		}
		return builder.build();
	}

	/** A boolean operand that counts its evaluations. */
	private static final class Operand implements Expression
	{
		private final char kind;
		private int evaluations;

		Operand(char kind) {
			this.kind = kind;
		}

		@Override
		public ValueExpTypeInfo getEvaluatesTo() {
			return XacmlTypes.BOOLEAN;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Expression> T evaluate(EvaluationContext context) {
			evaluations++;
			switch (kind) {
				case 'T':
					return (T) BooleanVal.of(true);
				case 'F':
					return (T) BooleanVal.of(false);
				case 'M':
					throw new EvaluationException(
							Status.missingAttribute("urn:test:attribute").build(), "operand is missing an attribute");
				default:
					throw new EvaluationException(Status.processingError().build(), "operand failed");
			}
		}

		@Override
		public void accept(ExpressionVisitor visitor) {
		}

		@Override
		public String toString() {
			return String.valueOf(kind);
		}
	}

	@XacmlFunctionProvider(description = "A predicate that fails for chosen arguments")
	public static final class FailingPredicates
	{
		/** Number of applications of the predicate since the last reset. */
		static int calls;

		private FailingPredicates() {}

		@XacmlFuncSpec(id = PREDICATE)
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal greaterThanOrFail(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
		{
			calls++;
			if (a.get() == -2 || b.get() == -2) {
				throw new EvaluationException(
						Status.missingAttribute("urn:test:attribute").build(), "operand is missing an attribute");
			}
			if (a.get() < 0 || b.get() < 0) {
				throw new EvaluationException(Status.processingError().build(), "negative operand");
			}
			return BooleanVal.of(a.get() > b.get());
		}
	}
}
