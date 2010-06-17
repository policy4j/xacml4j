package com.artagon.xacml.v3;



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
