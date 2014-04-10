package org.xacml4j.v30.policy.function;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;

public class AttributeDesignatorFunctionTest 
{
	private FunctionProvider func;
	
	@Before
	public void init() throws Exception{
		this.func = new AnnotiationBasedFunctionProvider(AttributeDesignatorFunctions.class);
	}
	
	@Test
	public void testDesignatorFunction(){
		
	}
}
