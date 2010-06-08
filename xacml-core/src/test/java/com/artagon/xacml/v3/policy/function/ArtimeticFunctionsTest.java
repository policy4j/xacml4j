package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

public class ArtimeticFunctionsTest 
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(XacmlDataTypes.INTEGER.create(3), 
				ArithmeticFunctions.addInteger(XacmlDataTypes.INTEGER.<IntegerValue>create(1), 
				XacmlDataTypes.INTEGER.<IntegerValue>create(2)));
	}
	
	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(XacmlDataTypes.DOUBLE.create(3.3 + 4.5), 
				ArithmeticFunctions.addDouble(XacmlDataTypes.DOUBLE.<DoubleValue>create(3.3), 
				XacmlDataTypes.DOUBLE.<DoubleValue>create(4.5)));
	}
	
	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(XacmlDataTypes.DOUBLE.create(2), 
				ArithmeticFunctions.divideInteger(XacmlDataTypes.INTEGER.<IntegerValue>create(4), 
				XacmlDataTypes.INTEGER.<IntegerValue>create(2)));
	}
}
