package org.xacml4j.v30.policy.function;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.DoubleType;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.IntegerType;



public class NumericComparisionFunctionTest
{
	@Test
	public void testGreatherThanDouble()
	{
		DoubleExp a = DoubleType.DOUBLE.create(35.0);
		DoubleExp b = DoubleType.DOUBLE.create(35.0);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleType.DOUBLE.create(35.1);
		b = DoubleType.DOUBLE.create(35.0);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleType.DOUBLE.create(35.1);
		b = DoubleType.DOUBLE.create(35.2);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanDouble(a, b));
	}
	
	@Test
	public void testGreatherThanOrEqualDouble()
	{
		DoubleExp a = DoubleType.DOUBLE.create(35.0);
		DoubleExp b = DoubleType.DOUBLE.create(35.0);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleType.DOUBLE.create(35.1);
		b = DoubleType.DOUBLE.create(35.0);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleType.DOUBLE.create(35.1);
		b = DoubleType.DOUBLE.create(35.2);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
	}
	
	@Test
	public void testGreatherThanInteger()
	{
		IntegerExp a = IntegerType.INTEGER.create(35);
		IntegerExp b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerType.INTEGER.create(36);
		b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerType.INTEGER.create(35);
		b = IntegerType.INTEGER.create(36);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanInteger(a, b));
	}
	
	@Test
	public void testGreatherThanOrEqualsInteger()
	{
		IntegerExp a = IntegerType.INTEGER.create(35);
		IntegerExp b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerType.INTEGER.create(36);
		b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerType.INTEGER.create(35);
		b = IntegerType.INTEGER.create(36);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
	}
}
