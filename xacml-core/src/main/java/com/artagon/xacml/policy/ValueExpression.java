package com.artagon.xacml.policy;

/**
 * An expression which evaluates to {@link Value}
 * instance
 * 
 * @author Giedrius Trumpickas
 */
public interface ValueExpression extends Expression
{
	/**
	 * Gets type to which this expression
	 * evaluates to
	 * 
	 * @return {@link ValueType}
	 */
	ValueType getEvaluatesTo();
}
