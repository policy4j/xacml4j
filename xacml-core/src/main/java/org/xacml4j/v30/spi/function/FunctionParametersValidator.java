package org.xacml4j.v30.spi.function;

import java.util.List;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionSpec;

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
