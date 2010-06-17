package com.artagon.xacml.v3;


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
