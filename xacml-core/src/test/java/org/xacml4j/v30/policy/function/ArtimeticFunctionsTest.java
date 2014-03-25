package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;

public class ArtimeticFunctionsTest
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(IntegerExp.valueOf(3),
				ArithmeticFunctions.addInteger(IntegerExp.valueOf(1),
						IntegerExp.valueOf(2)));
	}

	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(DoubleExp.valueOf(3.3 + 4.5),
				ArithmeticFunctions.addDouble(DoubleExp.valueOf(3.3),
						DoubleExp.valueOf(4.5)));
	}

	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(DoubleExp.valueOf(2),
				ArithmeticFunctions.divideInteger(IntegerExp.valueOf(4),
				IntegerExp.valueOf(2)));
	}
}
