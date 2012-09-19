package com.artagon.xacml.v30.spi.function;

import java.util.List;

import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.pdp.FunctionSpec;


public interface FunctionParametersValidator 
{
	/**
	 * Validates given function arguments
	 * 
	 * @param arguments a function arguments
	 * @return <code>true</code> if function
	 * arguments are valid
	 */
	boolean validate(FunctionSpec spec, List<Expression> arguments);
}
