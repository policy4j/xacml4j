package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;



public class NumericComparisionFunctionTest
{
	@Test
	public void testGreatherThanDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanDouble(a, b));
	}

	@Test
	public void testGreatherThanOrEqualDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
	}

	@Test
	public void testGreatherThanInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanInteger(a, b));
	}

	@Test
	public void testGreatherThanOrEqualsInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
	}
}
