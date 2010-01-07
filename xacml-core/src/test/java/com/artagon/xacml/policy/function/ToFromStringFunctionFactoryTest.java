package com.artagon.xacml.policy.function;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;


import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.FunctionFactory;

public class ToFromStringFunctionFactoryTest extends DefaultFunctionFactoryTestCase
{
	private FunctionFactory factory;
	
	@Before
	public void init(){
		this.factory = new ToFromStringFunctionFactory(types);
	}
	
	@Test
	public void testAllFunctions()
	{
		assertNotNull(factory.getFunction(Functions.ANYURI_FROM_STRING));
		assertNotNull(factory.getFunction(Functions.STRING_FROM_ANYURI));
	}
}
