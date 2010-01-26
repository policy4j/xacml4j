package com.artagon.xacml.v30.policy;

/**
 * Value expression represents literal
 * expression which evaluates to itself.
 * 
 * @author Giedrius Trumpickas
 */
public interface Value extends Expression
{	
	/**
	 * Returns {@link ValueType} representing
	 * type of this value
	 * 
	 * @return {@link ValueType} type for this value
	 */
	ValueType getType();
}
