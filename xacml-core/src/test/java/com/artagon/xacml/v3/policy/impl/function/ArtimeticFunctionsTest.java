package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

public class ArtimeticFunctionsTest 
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(DataTypes.INTEGER.create(3), 
				ArithmeticFunctions.addInteger(DataTypes.INTEGER.<IntegerValue>create(1), 
				DataTypes.INTEGER.<IntegerValue>create(2)));
	}
	
	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(DataTypes.DOUBLE.create(3.3 + 4.5), 
				ArithmeticFunctions.addDouble(DataTypes.DOUBLE.<DoubleValue>create(3.3), 
				DataTypes.DOUBLE.<DoubleValue>create(4.5)));
	}
	
	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(DataTypes.DOUBLE.create(2), 
				ArithmeticFunctions.divideInteger(DataTypes.INTEGER.<IntegerValue>create(4), 
				DataTypes.INTEGER.<IntegerValue>create(2)));
	}
}
