package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.policy.FunctionSpec;


/**
 * A function return type resolver
 * 
 * @author Giedrius Trumpickas
 */
public interface FunctionReturnTypeResolver 
{
	/**
	 * Resolves a function return type dynamically based
	 * on function invocation arguments
	 * 
	 * @param spec a function specification
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} function return type
	 */
	ValueType resolve(FunctionSpec spec, Expression ...arguments);
}
