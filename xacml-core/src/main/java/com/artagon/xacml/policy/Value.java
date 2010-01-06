package com.artagon.xacml.policy;

/**
 * Value expression represents literal
 * expression which evaluates to itself.
 * 
 * @author Giedrius Trumpickas
 */
public interface Value extends ValueExpression
{	
	/**
	 * Returns {@link ValueType} representing
	 * type of this value
	 * 
	 * @return {@link ValueType} type for this value
	 */
	ValueType getType();
}
