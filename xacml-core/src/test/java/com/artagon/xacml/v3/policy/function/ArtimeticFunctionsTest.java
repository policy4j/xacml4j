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
		assertEquals(IntegerType.Factory.create(3), 
				ArithmeticFunctions.addInteger(IntegerType.Factory.create(1), 
				IntegerType.Factory.create(2)));
	}
	
	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(DoubleType.Factory.create(3.3 + 4.5), 
				ArithmeticFunctions.addDouble(DoubleType.Factory.create(3.3), 
				DoubleType.Factory.create(4.5)));
	}
	
	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(DoubleType.Factory.create(2), 
				ArithmeticFunctions.divideInteger(IntegerType.Factory.create(4), 
				IntegerType.Factory.create(2)));
	}
}
