package com.artagon.xacml.v3.policy.function;

import org.junit.Test;

import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;

public class HigherOrderFunctionsTest 
{
	@Test
	public void testFunctionProvider()
	{
		new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class);
	}
}
