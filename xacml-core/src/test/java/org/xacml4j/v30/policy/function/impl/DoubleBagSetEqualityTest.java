package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML 3.0 Core OS A.3.1, A.3.10 and A.3.11: double bag and set
 * comparisons use double-equal, while Java equality stays reflexive.
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
public class DoubleBagSetEqualityTest
{
	private static final String F = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();
	private final EvaluationContext context = ModelFixtures.context(true);

	@ParameterizedTest(name = "double-equal({0}, {1}) = {2}")
	@MethodSource("scalarEqualityRows")
	public void doubleEqualUsesPrimitiveIeeeEquality(double left, double right, boolean expected)
	{
		DoubleVal first = DoubleVal.of(left);
		DoubleVal second = DoubleVal.of(right);
		assertNotSame(first, second);
		assertBoolean(expected, invoke("double-equal", first, second));
	}

	static Stream<Arguments> scalarEqualityRows()
	{
		return Stream.of(
				Arguments.of(0.0d, -0.0d, true),
				Arguments.of(-0.0d, 0.0d, true),
				Arguments.of(Double.NaN, Double.NaN, false),
				Arguments.of(Double.NaN, 1.0d, false),
				Arguments.of(1.0d, Double.NaN, false),
				Arguments.of(Double.NaN, Double.POSITIVE_INFINITY, false),
				Arguments.of(Double.POSITIVE_INFINITY, Double.NaN, false),
				Arguments.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true),
				Arguments.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true),
				Arguments.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, false),
				Arguments.of(42.125d, 42.125d, true),
				Arguments.of(42.125d, 42.0d, false),
				Arguments.of(Double.MIN_VALUE, 0.0d, false),
				Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, true));
	}

	@Test
	public void doubleEqualDoesNotMatchTheSameNaNObject()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		assertBoolean(false, invoke("double-equal", nan, nan));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("membershipRows")
	public void doubleIsInUsesTheSamePolicyEquality(String name, DoubleVal needle,
			BagOfValues haystack, boolean expected)
	{
		assertBoolean(expected, invoke("double-is-in", needle, haystack));
	}

	static Stream<Arguments> membershipRows()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		return Stream.of(
				Arguments.of("positive zero in negative zeros", DoubleVal.of(0.0d), doubles(-0.0d, -0.0d), true),
				Arguments.of("negative zero in positive zero", DoubleVal.of(-0.0d), doubles(0.0d), true),
				Arguments.of("same NaN object is not a member", nan, bag(nan), false),
				Arguments.of("distinct NaN wrappers are not members", nan, doubles(Double.NaN), false),
				Arguments.of("NaN does not match mixed values", nan, doubles(Double.NaN, 1.0d), false),
				Arguments.of("finite wrapper matches through NaN", DoubleVal.of(1.0d), doubles(Double.NaN, 1.0d), true),
				Arguments.of("empty bag has no finite member", DoubleVal.of(1.0d), doubles(), false),
				Arguments.of("empty bag has no NaN member", nan, doubles(), false),
				Arguments.of("unequal finite numbers", DoubleVal.of(1.0d), doubles(2.0d), false),
				Arguments.of("duplicate finite numbers", DoubleVal.of(2.0d), doubles(2.0d, 2.0d), true),
				Arguments.of("positive infinity matches", DoubleVal.of(Double.POSITIVE_INFINITY),
						doubles(Double.POSITIVE_INFINITY), true));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("bagRelationRows")
	public void doubleBagRelationsUseTheSamePolicyEquality(String name, String function,
			BagOfValues left, BagOfValues right, boolean expected)
	{
		assertBoolean(expected, invoke("double-" + function, left, right));
	}

	static Stream<Arguments> bagRelationRows()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		BagOfValues sameNaNBag = bag(nan);
		return Stream.of(
				relations("zero signs", doubles(0.0d), doubles(-0.0d), true, true, true),
				relations("reversed zero signs", doubles(-0.0d), doubles(0.0d), true, true, true),
				relations("duplicate zero signs", doubles(-0.0d, 0.0d, -0.0d), doubles(0.0d), true, true, true),
				relations("distinct finite wrappers", doubles(42.125d), doubles(42.125d), true, true, true),
				relations("duplicate finite values", doubles(1.0d, 1.0d), doubles(1.0d), true, true, true),
				relations("strict subset", doubles(1.0d), doubles(1.0d, 2.0d), true, false, true),
				relations("unequal finite values", doubles(1.0d), doubles(2.0d), false, false, false),
				relations("both empty", doubles(), doubles(), true, true, false),
				relations("empty left", doubles(), doubles(1.0d), true, false, false),
				relations("empty right", doubles(1.0d), doubles(), false, false, false),
				relations("same NaN bag and object", sameNaNBag, sameNaNBag, false, false, false),
				relations("distinct NaN wrappers", doubles(Double.NaN), doubles(Double.NaN), false, false, false),
				relations("NaNs mixed with a common finite value", bag(nan, DoubleVal.of(1.0d)),
						bag(nan, DoubleVal.of(1.0d)), false, false, true),
				relations("finite member of a NaN bag", doubles(1.0d), doubles(1.0d, Double.NaN), true, false, true),
				relations("duplicate infinities", doubles(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
						doubles(Double.POSITIVE_INFINITY), true, true, true))
				.flatMap(rows -> rows);
	}

	private static Stream<Arguments> relations(String name, BagOfValues left, BagOfValues right,
			boolean subset, boolean setEquals, boolean overlaps)
	{
		return Stream.of(
				Arguments.of(name + " subset", "subset", left, right, subset),
				Arguments.of(name + " set-equals", "set-equals", left, right, setEquals),
				Arguments.of(name + " at-least-one-member-of", "at-least-one-member-of", left, right, overlaps));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("setResultRows")
	public void doubleSetResultsUsePolicyEqualityAndKeepUnmatchedNaNOccurrences(String name,
			String function, BagOfValues left, BagOfValues right, double[] expected)
	{
		BagOfValues result = assertInstanceOf(BagOfValues.class, invoke("double-" + function, left, right));
		assertDoubleBag(result, expected);
	}

	static Stream<Arguments> setResultRows()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		BagOfValues repeatedNaN = bag(nan, nan);
		BagOfValues mixed = bag(nan, nan, DoubleVal.of(-0.0d), DoubleVal.of(0.0d),
				DoubleVal.of(1.0d), DoubleVal.of(1.0d));
		return Stream.of(
				Arguments.of("union collapses all zero signs", "union", doubles(0.0d, -0.0d, -0.0d),
						doubles(-0.0d, 0.0d), new double[]{0.0d}),
				Arguments.of("intersection collapses all zero signs", "intersection", doubles(0.0d, -0.0d),
						doubles(-0.0d, -0.0d), new double[]{0.0d}),
				Arguments.of("finite union removes duplicates", "union", doubles(2.0d, 2.0d),
						doubles(2.0d, 3.0d), new double[]{2.0d, 3.0d}),
				Arguments.of("finite intersection removes duplicates", "intersection", doubles(2.0d, 2.0d),
						doubles(2.0d, 2.0d, 3.0d), new double[]{2.0d}),
				Arguments.of("union retains repeated same-object NaNs", "union", repeatedNaN, bag(nan),
						new double[]{Double.NaN, Double.NaN, Double.NaN}),
				Arguments.of("intersection excludes same-object NaNs", "intersection", repeatedNaN, bag(nan),
						new double[]{}),
				Arguments.of("union does not shortcut the same NaN bag", "union", repeatedNaN, repeatedNaN,
						new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN}),
				Arguments.of("intersection does not shortcut the same NaN bag", "intersection", repeatedNaN, repeatedNaN,
						new double[]{}),
				Arguments.of("union retains distinct NaN wrappers", "union", doubles(Double.NaN), doubles(Double.NaN),
						new double[]{Double.NaN, Double.NaN}),
				Arguments.of("intersection excludes distinct NaN wrappers", "intersection", doubles(Double.NaN),
						doubles(Double.NaN), new double[]{}),
				Arguments.of("mixed union preserves NaNs and removes numeric duplicates", "union", mixed,
						bag(nan, DoubleVal.of(-0.0d), DoubleVal.of(1.0d), DoubleVal.of(2.0d)),
						new double[]{Double.NaN, Double.NaN, Double.NaN, 0.0d, 1.0d, 2.0d}),
				Arguments.of("mixed intersection excludes NaNs", "intersection", mixed,
						bag(nan, DoubleVal.of(-0.0d), DoubleVal.of(1.0d), DoubleVal.of(2.0d)),
						new double[]{0.0d, 1.0d}),
				Arguments.of("union of empty bags is typed empty", "union", doubles(), doubles(), new double[]{}),
				Arguments.of("intersection of empty bags is typed empty", "intersection", doubles(), doubles(), new double[]{}),
				Arguments.of("union normalizes the left bag when right is empty", "union", mixed, doubles(),
						new double[]{Double.NaN, Double.NaN, 0.0d, 1.0d}),
				Arguments.of("union normalizes the right bag when left is empty", "union", doubles(), mixed,
						new double[]{Double.NaN, Double.NaN, 0.0d, 1.0d}),
				Arguments.of("intersection with empty right", "intersection", mixed, doubles(), new double[]{}),
				Arguments.of("intersection with empty left", "intersection", doubles(), mixed, new double[]{}),
				Arguments.of("disjoint union", "union", doubles(1.0d), doubles(2.0d), new double[]{1.0d, 2.0d}),
				Arguments.of("disjoint intersection", "intersection", doubles(1.0d), doubles(2.0d), new double[]{}),
				Arguments.of("infinity union removes duplicates", "union", doubles(Double.POSITIVE_INFINITY),
						doubles(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
						new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}),
				Arguments.of("infinity intersection removes duplicates", "intersection", doubles(Double.POSITIVE_INFINITY),
						doubles(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
						new double[]{Double.POSITIVE_INFINITY}));
	}

	@Test
	public void doubleBagAndBagSizePreserveDuplicatesAndZeroSigns()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		BagOfValues result = assertInstanceOf(BagOfValues.class, invoke("double-bag",
				DoubleVal.of(0.0d), DoubleVal.of(-0.0d), DoubleVal.of(-0.0d),
				nan, nan, DoubleVal.of(1.0d), DoubleVal.of(1.0d)));
		assertDoubleBag(result, 0.0d, 0.0d, 0.0d, Double.NaN, Double.NaN, 1.0d, 1.0d);
		assertEquals(IntegerVal.of(7), invoke("double-bag-size", result));
		assertEquals(1L, result.<DoubleVal>stream()
				.filter(value -> Double.doubleToRawLongBits(value.get()) == Double.doubleToRawLongBits(0.0d)).count());
		assertEquals(2L, result.<DoubleVal>stream()
				.filter(value -> Double.doubleToRawLongBits(value.get()) == Double.doubleToRawLongBits(-0.0d)).count());
		BagOfValues empty = assertInstanceOf(BagOfValues.class, invoke("double-bag"));
		assertDoubleBag(empty);
		assertEquals(IntegerVal.of(0), invoke("double-bag-size", empty));
	}

	@Test
	public void javaEqualityAndHashCollectionsRemainReflexive()
	{
		DoubleVal nan = DoubleVal.of(Double.NaN);
		DoubleVal otherNaN = DoubleVal.of(Double.longBitsToDouble(0x7ff8000000000001L));
		assertNotSame(nan, otherNaN);
		assertEquals(nan, nan);
		assertEquals(nan, otherNaN);
		assertEquals(nan.hashCode(), otherNaN.hashCode());
		assertEquals(1, new HashSet<>(List.of(nan, otherNaN)).size());
		Map<DoubleVal, String> values = new HashMap<>();
		values.put(nan, "nan");
		assertEquals("nan", values.get(nan));
		assertEquals("nan", values.get(otherNaN));

		DoubleVal positiveZero = DoubleVal.of(0.0d);
		DoubleVal negativeZero = DoubleVal.of(-0.0d);
		assertNotEquals(positiveZero, negativeZero);
		values.put(positiveZero, "positive zero");
		values.put(negativeZero, "negative zero");
		assertEquals(3, values.size());
		assertEquals("positive zero", values.get(DoubleVal.of(0.0d)));
		assertEquals("negative zero", values.get(DoubleVal.of(-0.0d)));

		BagOfValues repeated = bag(nan, nan, positiveZero, negativeZero);
		BagOfValues equalBag = bag(otherNaN, otherNaN, DoubleVal.of(0.0d), DoubleVal.of(-0.0d));
		assertEquals(repeated, repeated);
		assertEquals(repeated, equalBag);
		assertEquals(repeated.hashCode(), equalBag.hashCode());
		assertEquals(1, new HashSet<>(List.of(repeated, equalBag)).size());
		assertNotEquals(repeated, bag(nan, positiveZero, negativeZero));
		assertEquals(4, repeated.size());
	}

	@Test
	public void integerPolicyEqualityKeepsAdjacentValuesAboveDoublePrecisionDistinct()
	{
		// Integer functions must not pass their values through the double predicate.
		long lower = 9007199254740992L;
		long upper = 9007199254740993L;
		IntegerVal first = IntegerVal.of(upper);
		IntegerVal sameNumber = IntegerVal.of(upper);
		assertNotSame(first, sameNumber);
		assertBoolean(true, invoke("integer-equal", first, sameNumber));
		assertBoolean(false, invoke("integer-equal", IntegerVal.of(lower), first));
		BagOfValues left = XacmlTypes.INTEGER.bagOf(IntegerVal.of(lower), IntegerVal.of(lower));
		BagOfValues right = XacmlTypes.INTEGER.bagOf(first, sameNumber);
		assertBoolean(false, invoke("integer-is-in", first, left));
		assertBoolean(true, invoke("integer-is-in", sameNumber, right));
		for (String function : List.of("subset", "set-equals", "at-least-one-member-of")) {
			assertBoolean(false, invoke("integer-" + function, left, right));
			assertBoolean(true, invoke("integer-" + function, right, XacmlTypes.INTEGER.bagOf(sameNumber)));
		}
		BagOfValues intersection = assertInstanceOf(BagOfValues.class, invoke("integer-intersection", left, right));
		assertEquals(XacmlTypes.INTEGER, intersection.getBagValueType());
		assertTrue(intersection.isEmpty());
		BagOfValues union = assertInstanceOf(BagOfValues.class, invoke("integer-union", left, right));
		assertEquals(XacmlTypes.INTEGER, union.getBagValueType());
		assertEquals(2, union.size());
		assertArrayEquals(new long[]{lower, upper}, union.<IntegerVal>stream()
				.mapToLong(value -> value.get()).sorted().toArray());
	}

	@ParameterizedTest(name = "double-{0} rejects integer bags")
	@MethodSource("doubleBagComparisonFunctions")
	public void doubleFunctionsRejectWrongBagTypesAtRuntime(String function)
	{
		FunctionSpec spec = require("double-" + function);
		Expression left = function.equals("is-in") ? DoubleVal.of(1.0d) : doubles(1.0d);
		BagOfValues integers = XacmlTypes.INTEGER.bagOf(IntegerVal.of(1));
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> spec.invoke(context, left, integers));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, error.getStatus().getStatusCode().getValue());

		// The PDP normally disables generic parameter validation. These rows
		// exercise the function's own type guard, including the empty-bag path.
		EvaluationContext unchecked = ModelFixtures.context(false);
		for (BagOfValues wrongType : List.of(integers, XacmlTypes.INTEGER.bagOf())) {
			EvaluationException uncheckedError = assertThrows(EvaluationException.class,
					() -> spec.invoke(unchecked, left, wrongType));
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR,
					uncheckedError.getStatus().getStatusCode().getValue());
		}
	}

	@ParameterizedTest(name = "double-{0} rejects missing arguments")
	@MethodSource("doubleBagComparisonFunctions")
	public void doubleFunctionsRejectWrongArityAtRuntime(String function)
	{
		FunctionSpec spec = require("double-" + function);
		Expression left = function.equals("is-in") ? DoubleVal.of(1.0d) : doubles(1.0d);
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> spec.invoke(context, left));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, error.getStatus().getStatusCode().getValue());
	}

	static Stream<String> doubleBagComparisonFunctions()
	{
		return Stream.of("is-in", "subset", "set-equals", "intersection", "union", "at-least-one-member-of");
	}

	private ValueExp invoke(String name, Expression... arguments)
	{
		return require(name).invoke(context, arguments);
	}

	private static FunctionSpec require(String name)
	{
		return FUNCTIONS.getFunction(F + name)
				.orElseThrow(() -> new AssertionError("missing function " + F + name));
	}

	private static void assertBoolean(boolean expected, ValueExp result)
	{
		assertEquals(expected, assertInstanceOf(BooleanVal.class, result).get().booleanValue());
	}

	private static void assertDoubleBag(BagOfValues actual, double... expected)
	{
		// Independent data checks: never use BagOfValues.equals or a policy
		// function under test as the oracle. Do not pick a surviving zero sign.
		assertEquals(XacmlTypes.DOUBLE, actual.getBagValueType());
		assertEquals(expected.length, actual.size());
		double[] values = actual.<DoubleVal>stream().mapToDouble(value -> value.get()).toArray();
		assertEquals(Arrays.stream(expected).filter(Double::isNaN).count(),
				Arrays.stream(values).filter(Double::isNaN).count());
		assertEquals(Arrays.stream(expected).filter(value -> value == 0.0d).count(),
				Arrays.stream(values).filter(value -> value == 0.0d).count());
		assertArrayEquals(Arrays.stream(expected).filter(value -> !Double.isNaN(value) && value != 0.0d).sorted().toArray(),
				Arrays.stream(values).filter(value -> !Double.isNaN(value) && value != 0.0d).sorted().toArray());
	}

	private static BagOfValues bag(DoubleVal... values)
	{
		return XacmlTypes.DOUBLE.bagOf(values);
	}

	private static BagOfValues doubles(double... values)
	{
		BagOfValues.Builder builder = XacmlTypes.DOUBLE.bagBuilder();
		for (double value : values) {
			builder.attribute(DoubleVal.of(value));
		}
		return builder.build();
	}
}
