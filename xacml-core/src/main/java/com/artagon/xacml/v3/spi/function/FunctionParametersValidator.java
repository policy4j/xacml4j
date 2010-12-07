package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;


public interface FunctionParametersValidator 
{
	/**
	 * Validates given function arguments
	 * 
	 * @param arguments a function arguments
	 * @return <code>true</code> if function
	 * arguments are valid
	 */
	boolean validate(FunctionSpec spec, Expression ...arguments);
}
