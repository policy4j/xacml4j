package com.artagon.xacml.policy;


/**
 * An XACML function with variable return type
 * based on the function invocation parameters
 * 
 * @author Giedrius Trumpickas
 */
public interface DynamicFunction extends Function
{
	ValueType resolveReturnType(Expression ...parameters);
}
