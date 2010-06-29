package com.artagon.xacml.v3.policy.function;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;

public class SpecialMatchFunctionsTest 
{
	private FunctionProvider p;
	
	@Before
	public void init(){
		this.p = new AnnotiationBasedFunctionProvider(SpecialMatchFunctions.class);
	}
	
	@Test
	public void testIfFunctionImplemented()
	{
		
	}
}
