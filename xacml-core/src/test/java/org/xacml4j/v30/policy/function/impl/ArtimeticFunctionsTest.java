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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;

public class ArtimeticFunctionsTest
{
	private static final String F = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(XacmlTypes.INTEGER.ofAny(3),
		             ArithmeticFunctions.addInteger(XacmlTypes.INTEGER.ofAny(1),
		                                            XacmlTypes.INTEGER.ofAny(2)));
	}

	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(3.3 + 4.5),
				ArithmeticFunctions.addDouble(XacmlTypes.DOUBLE.ofAny(3.3),
						XacmlTypes.DOUBLE.ofAny(4.5)));
	}

	@Test
	public void testDivideIntegerFunction()
	{
		// XACML 3.0 Core OS A.3.2: integer arithmetic returns xs:integer.
		ValueExp result = require(F + "integer-divide").invoke(ModelFixtures.context(true),
				IntegerVal.of(4), IntegerVal.of(2));
		assertEquals(XacmlTypes.INTEGER, result.getEvaluatesTo());
		assertEquals(IntegerVal.of(2), result);
	}

	@ParameterizedTest(name = "{0} declares xs:integer")
	@MethodSource("integerDivideIds")
	public void integerDivideDeclaresAnIntegerResult(String functionId)
	{
		assertEquals(XacmlTypes.INTEGER,
				require(functionId).resolveReturnType(List.of()).getValueType());
	}

	static Stream<String> integerDivideIds()
	{
		return Stream.of(F + "integer-divide");
	}

	@ParameterizedTest(name = "{0} / {1} = {2}")
	@MethodSource("integerDivisionRows")
	public void integerDivideReturnsTheExactInteger(long dividend, long divisor, long expected)
	{
		// A.3.2 requires the integer result type. Toward-zero rounding is
		// approved PROJECT behavior; A.3.2 does not expressly prescribe it.
		ValueExp result = require(F + "integer-divide").invoke(ModelFixtures.context(true),
				IntegerVal.of(dividend), IntegerVal.of(divisor));
		IntegerVal quotient = assertInstanceOf(IntegerVal.class, result);
		assertEquals(XacmlTypes.INTEGER, quotient.getEvaluatesTo());
		assertEquals(expected, quotient.get().longValue());
	}

	static Stream<Arguments> integerDivisionRows()
	{
		return Stream.of(
				Arguments.of(4L, 2L, 2L),
				Arguments.of(-4L, 2L, -2L),
				Arguments.of(4L, -2L, -2L),
				Arguments.of(-4L, -2L, 2L),
				Arguments.of(5L, 2L, 2L),
				Arguments.of(3L, 2L, 1L),
				Arguments.of(-3L, 2L, -1L),
				Arguments.of(7L, 2L, 3L),
				Arguments.of(-7L, 2L, -3L),
				Arguments.of(7L, -3L, -2L),
				Arguments.of(-7L, -3L, 2L),
				Arguments.of(0L, 5L, 0L),
				Arguments.of(0L, -5L, 0L),
				Arguments.of(9007199254740993L, 1L, 9007199254740993L),
				Arguments.of(18014398509481986L, 2L, 9007199254740993L),
				Arguments.of(-18014398509481986L, 2L, -9007199254740993L),
				Arguments.of(Long.MAX_VALUE, 1L, Long.MAX_VALUE),
				Arguments.of(Long.MIN_VALUE, 1L, Long.MIN_VALUE));
	}

	@Test
	public void integerDivisionComposesWithAnIntegerComparisonUnderRuntimeValidation()
	{
		// IIC020's production Apply shape, with runtime type validation enabled.
		EvaluationContext context = ModelFixtures.context(true);
		assertTrue(context.isValidateFuncParamsAtRuntime());
		Apply quotient = Apply.builder(require(F + "integer-divide"))
				.param(IntegerVal.of(45), IntegerVal.of(2)).build();
		Apply comparison = Apply.builder(require(F + "integer-greater-than-or-equal"))
				.param(quotient, IntegerVal.of(10)).build();
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), comparison.evaluate(context));
	}

	@ParameterizedTest(name = "{0}({1}, {2}) is processing-error")
	@MethodSource("zeroDivisorRows")
	public void zeroDivisorsMapToProcessingError(String functionId, ValueExp dividend, ValueExp divisor)
	{
		// A.3.2 and the change's zero-divisor requirement: use the registry wrapper.
		FunctionSpec function = require(functionId);
		EvaluationContext context = ModelFixtures.context(true);
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> function.invoke(context, dividend, divisor));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, error.getStatus().getStatusCode().getValue());
	}

	static Stream<Arguments> zeroDivisorRows()
	{
		return Stream.of(
				Arguments.of(F + "integer-divide", IntegerVal.of(1), IntegerVal.of(0)),
				Arguments.of(F + "integer-divide", IntegerVal.of(0), IntegerVal.of(0)),
				Arguments.of(F + "integer-mod", IntegerVal.of(5), IntegerVal.of(0)),
				Arguments.of(F + "integer-mod", IntegerVal.of(0), IntegerVal.of(0)),
				Arguments.of(F + "double-divide", XacmlTypes.DOUBLE.ofAny(1.0d), XacmlTypes.DOUBLE.ofAny(0.0d)),
				Arguments.of(F + "double-divide", XacmlTypes.DOUBLE.ofAny(1.0d), XacmlTypes.DOUBLE.ofAny(-0.0d)),
				Arguments.of(F + "double-divide", XacmlTypes.DOUBLE.ofAny(0.0d), XacmlTypes.DOUBLE.ofAny(0.0d)),
				Arguments.of(F + "double-divide", XacmlTypes.DOUBLE.ofAny(0.0d), XacmlTypes.DOUBLE.ofAny(-0.0d)));
	}

	@Test
	public void zeroDivisionRemainsProcessingErrorInsideAComposedApply()
	{
		Apply quotient = Apply.builder(require(F + "integer-divide"))
				.param(IntegerVal.of(1), IntegerVal.of(0)).build();
		Apply comparison = Apply.builder(require(F + "integer-greater-than-or-equal"))
				.param(quotient, IntegerVal.of(10)).build();
		EvaluationContext context = ModelFixtures.context(true);
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> comparison.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, error.getStatus().getStatusCode().getValue());
	}

	@Test
	public void integerDivideRejectsWrongArityAndTypesThroughTheRegistry()
	{
		FunctionSpec function = require(F + "integer-divide");
		EvaluationContext context = ModelFixtures.context(true);
		for (Expression[] arguments : List.of(
				new Expression[]{},
				new Expression[]{IntegerVal.of(4)},
				new Expression[]{IntegerVal.of(4), IntegerVal.of(2), IntegerVal.of(1)},
				new Expression[]{XacmlTypes.DOUBLE.ofAny(4.0d), IntegerVal.of(2)},
				new Expression[]{IntegerVal.of(4), XacmlTypes.INTEGER.bagOf(IntegerVal.of(2))})) {
			EvaluationException error = assertThrows(EvaluationException.class,
					() -> function.invoke(context, arguments));
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, error.getStatus().getStatusCode().getValue());
		}
	}

	private static FunctionSpec require(String functionId)
	{
		return FUNCTIONS.getFunction(functionId)
				.orElseThrow(() -> new AssertionError("missing function " + functionId));
	}
}
