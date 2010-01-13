package com.artagon.xacml.policy;

import java.util.List;

/**
 * An XACML function with variable return type
 * based on the function invocation parameters
 * 
 * @author Giedrius Trumpickas
 */
public interface DynamicFunction extends Function
{
	ValueType resolveReturnType(List<Expression> arguments);
}
