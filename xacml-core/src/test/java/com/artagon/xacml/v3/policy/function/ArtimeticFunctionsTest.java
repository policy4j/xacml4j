package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.IntegerType;

public class ArtimeticFunctionsTest 
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(IntegerType.INTEGER.create(3), 
				ArithmeticFunctions.addInteger(IntegerType.INTEGER.create(1), 
				IntegerType.INTEGER.create(2)));
	}
	
	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(DoubleType.DOUBLE.create(3.3 + 4.5), 
				ArithmeticFunctions.addDouble(DoubleType.DOUBLE.create(3.3), 
				DoubleType.DOUBLE.create(4.5)));
	}
	
	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(DoubleType.DOUBLE.create(2), 
				ArithmeticFunctions.divideInteger(IntegerType.INTEGER.create(4), 
				IntegerType.INTEGER.create(2)));
	}
}
