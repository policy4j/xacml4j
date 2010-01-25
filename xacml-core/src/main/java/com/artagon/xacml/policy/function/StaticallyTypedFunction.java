package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.Function;
import com.artagon.xacml.policy.ValueType;

/**
 * An XACML function with statically defined
 * return type.
 * 
 * @author Giedrius Trumpickas
 */
public interface StaticallyTypedFunction extends Function 
{
	ValueType getReturnType();
}
