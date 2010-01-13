package com.artagon.xacml.policy;

/**
 * An XACML function with statically defined
 * return type.
 * 
 * @author Giedrius Trumpickas
 */
public interface RegularFunction extends Function 
{
	ValueType getReturnType();
}
