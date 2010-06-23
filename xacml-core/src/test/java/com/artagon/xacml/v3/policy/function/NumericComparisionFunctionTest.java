package com.artagon.xacml.v3.policy.function;

import static junit.framework.Assert.*;

import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;


public class NumericComparisionFunctionTest
{
	@Test
	public void testGreatherThanDouble()
	{
		DoubleValue a = XacmlDataTypes.DOUBLE.create(35.0);
		DoubleValue b = XacmlDataTypes.DOUBLE.create(35.0);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = XacmlDataTypes.DOUBLE.create(35.1);
		b = XacmlDataTypes.DOUBLE.create(35.0);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = XacmlDataTypes.DOUBLE.create(35.1);
		b = XacmlDataTypes.DOUBLE.create(35.2);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanDouble(a, b));
	}
	
	@Test
	public void testGreatherThanOrEqualDouble()
	{
		DoubleValue a = XacmlDataTypes.DOUBLE.create(35.0);
		DoubleValue b = XacmlDataTypes.DOUBLE.create(35.0);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = XacmlDataTypes.DOUBLE.create(35.1);
		b = XacmlDataTypes.DOUBLE.create(35.0);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = XacmlDataTypes.DOUBLE.create(35.1);
		b = XacmlDataTypes.DOUBLE.create(35.2);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
	}
	
	@Test
	public void testGreatherThanInteger()
	{
		IntegerValue a = XacmlDataTypes.INTEGER.create(35);
		IntegerValue b = XacmlDataTypes.INTEGER.create(35);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = XacmlDataTypes.INTEGER.create(36);
		b = XacmlDataTypes.INTEGER.create(35);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = XacmlDataTypes.INTEGER.create(35);
		b = XacmlDataTypes.INTEGER.create(36);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanInteger(a, b));
	}
	
	@Test
	public void testGreatherThanOrEqualsInteger()
	{
		IntegerValue a = XacmlDataTypes.INTEGER.create(35);
		IntegerValue b = XacmlDataTypes.INTEGER.create(35);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = XacmlDataTypes.INTEGER.create(36);
		b = XacmlDataTypes.INTEGER.create(35);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = XacmlDataTypes.INTEGER.create(35);
		b = XacmlDataTypes.INTEGER.create(36);
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
	}
}
