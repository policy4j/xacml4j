package com.artagon.xacml.v30.policy.function;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.DoubleValue;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.IntegerValue;


public class NumericComparisionFunctionTest
{
	@Test
	public void testGreatherThanDouble()
	{
		DoubleValue a = DoubleType.DOUBLE.create(35.0);
		DoubleValue b = DoubleType.DOUBLE.create(35.0);
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
		DoubleValue a = DoubleType.DOUBLE.create(35.0);
		DoubleValue b = DoubleType.DOUBLE.create(35.0);
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
		IntegerValue a = IntegerType.INTEGER.create(35);
		IntegerValue b = IntegerType.INTEGER.create(35);
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
		IntegerValue a = IntegerType.INTEGER.create(35);
		IntegerValue b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerType.INTEGER.create(36);
		b = IntegerType.INTEGER.create(35);
		assertEquals(BooleanType.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerType.INTEGER.create(35);
		b = IntegerType.INTEGER.create(36);
		assertEquals(BooleanType.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
	}
}
