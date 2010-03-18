package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Expression;

public interface FunctionParametersValidator 
{
	/**
	 * Validates given function arguments
	 * 
	 * @param arguments a function arguments
	 * @return <code>true</code> if function
	 * arguments are valid
	 */
	boolean validate(Expression ...arguments);
}
