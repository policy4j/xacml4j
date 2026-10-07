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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.IntegerVal;

/**
 * Maintainer ruling 2026-10-07 (run 061 D4, bead policy4j-the.4.59): the
 * integer arithmetic functions work on 64-bit signed integers. A result
 * outside that range is Indeterminate with status processing-error. It is
 * never a wrapped value.
 */
public class IntegerOverflowTest
{
	private static final String F = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final long MAX = Long.MAX_VALUE;
	private static final long MIN = Long.MIN_VALUE;
	/** 2^62 */
	private static final long TWO_62 = 1L << 62;

	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	@ParameterizedTest(name = "{0}{1} = {2}")
	@MethodSource("resultsInsideTheRange")
	public void resultInsideTheRangeIsReturned(String function, String operandsText, long expected, long[] operands)
	{
		assertEquals(IntegerVal.of(expected), invoke(function, operands));
	}

	static Stream<Arguments> resultsInsideTheRange()
	{
		return Stream.of(
				inside("integer-add", MAX, MAX, 0),
				inside("integer-add", MIN, MIN, 0),
				inside("integer-add", MAX, MAX - 1, 1),
				inside("integer-add", -1, MAX, MIN),
				// The first two operands leave the range; the sum of all three is inside it.
				inside("integer-add", MAX, MAX, 1, -1),
				inside("integer-add", MIN, MIN, -1, 1),
				inside("integer-subtract", MAX, -1, MIN),
				inside("integer-subtract", MIN, -1, MAX),
				inside("integer-subtract", 0, MIN, MIN),
				inside("integer-multiply", MIN, MIN, 1),
				inside("integer-multiply", MAX, MAX, 1),
				inside("integer-multiply", -MAX, MAX, -1),
				inside("integer-multiply", MIN, TWO_62, -2),
				// The largest square inside the range; 3037000500 squared is outside it.
				inside("integer-multiply", 9223372030926249001L, 3037000499L, 3037000499L),
				// The first two operands leave the range; the product of all three is inside it.
				inside("integer-multiply", 0, MAX, MAX, 0),
				inside("integer-multiply", 0, MIN, -1, 0),
				inside("integer-abs", MAX, MAX),
				inside("integer-abs", MAX, MIN + 1),
				inside("integer-abs", 0, 0),
				inside("integer-divide", MIN, MIN, 1),
				inside("integer-divide", -MAX, MAX, -1),
				inside("integer-divide", 1, MIN, MIN),
				inside("integer-divide", 0, MAX, MIN),
				// The remainder of MIN by -1 is 0; only the quotient is outside the range.
				inside("integer-mod", 0, MIN, -1),
				inside("integer-mod", -1, MIN, MAX),
				inside("integer-mod", MAX, MAX, MIN));
	}

	@ParameterizedTest(name = "{0}{1} is Indeterminate")
	@MethodSource("resultsOutsideTheRange")
	public void resultOutsideTheRangeIsIndeterminate(String function, String operandsText, long[] operands)
	{
		EvaluationException e = assertThrows(EvaluationException.class, () -> invoke(function, operands));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		// The message names the function, not the operands.
		assertEquals(Optional.of(function + " result is outside the xs:integer range"), e.getStatus().getMessage());
		// The arithmetic failure stays in the cause chain for the log.
		Throwable cause = e;
		while (cause != null && !(cause instanceof ArithmeticException)) {
			cause = cause.getCause();
		}
		assertNotNull(cause, "no ArithmeticException in the cause chain");
	}

	static Stream<Arguments> resultsOutsideTheRange()
	{
		return Stream.of(
				outside("integer-add", MAX, 1),
				outside("integer-add", MIN, -1),
				outside("integer-add", MAX, MAX),
				outside("integer-add", MIN, MIN),
				outside("integer-add", MAX, 1, 0),
				outside("integer-add", 1, 0, MAX),
				outside("integer-subtract", MIN, 1),
				outside("integer-subtract", MAX, -1),
				outside("integer-subtract", 0, MIN),
				outside("integer-multiply", MIN, -1),
				outside("integer-multiply", MAX, 2),
				outside("integer-multiply", 1L << 32, 1L << 32),
				outside("integer-multiply", 3037000500L, 3037000500L),
				outside("integer-multiply", TWO_62, 2),
				outside("integer-multiply", 2, 2, TWO_62),
				outside("integer-abs", MIN),
				outside("integer-divide", MIN, -1));
	}

	/** The methods of the value type do not wrap either; they are not XACML functions and throw. */
	@Test
	public void integerValueArithmeticDoesNotWrap()
	{
		IntegerVal max = IntegerVal.of(MAX);
		IntegerVal min = IntegerVal.of(MIN);
		IntegerVal one = IntegerVal.of(1);

		assertEquals(max, IntegerVal.of(MAX - 1).add(one));
		assertEquals(min, IntegerVal.of(MIN + 1).subtract(one));
		assertEquals(min, min.multiply(one));
		assertEquals(max, IntegerVal.of(MIN + 1).abs());
		assertThrows(ArithmeticException.class, () -> max.add(one));
		assertThrows(ArithmeticException.class, () -> min.subtract(one));
		assertThrows(ArithmeticException.class, () -> max.multiply(IntegerVal.of(2)));
		assertThrows(ArithmeticException.class, () -> min.multiply(IntegerVal.of(-1)));
		assertThrows(ArithmeticException.class, min::abs);
	}

	private static Arguments inside(String function, long expected, long... operands)
	{
		return Arguments.of(function, Arrays.toString(operands), expected, operands);
	}

	private static Arguments outside(String function, long... operands)
	{
		return Arguments.of(function, Arrays.toString(operands), operands);
	}

	private static Object invoke(String function, long... operands)
	{
		Expression[] arguments = new Expression[operands.length];
		for (int i = 0; i < operands.length; i++) {
			arguments[i] = IntegerVal.of(operands[i]);
		}
		return FUNCTIONS.getFunction(F + function)
				.orElseThrow(() -> new AssertionError("missing function " + function))
				.invoke(mock(EvaluationContext.class), arguments);
	}
}
