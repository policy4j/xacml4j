package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.policy.Expression;
import com.artagon.xacml.v30.policy.Function;
import com.artagon.xacml.v30.policy.ValueType;


/**
 * An XACML function with variable return type
 * based on the function invocation parameters
 * 
 * @author Giedrius Trumpickas
 */
public interface DynamicallyTypedFunction extends Function
{
	/**
	 * Resolves function return type based on given
	 * function invocation arguments
	 * 
	 * @param arguments a function invocation
	 * arguments
	 * @return {@link ValueType} function return type
	 */
	ValueType resolveReturnType(Expression ...parameters);
}
