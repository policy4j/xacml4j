package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.Function;
import com.artagon.xacml.policy.ValueType;


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
