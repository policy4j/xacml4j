package com.artagon.xacml.policy;

public interface Attribute extends Value
{
	/**
	 * Converts this XACML attribute value
	 * to {@link String}
	 * 
	 * @return this attribute value as {@link String}
	 */
	String toXacmlString();
	
	/**
	 * Implementation of this method 
	 * returns reference to itself
	 * 
	 * @see {@link Expression#evaluate(EvaluationContext)}
	 */
	Attribute evaluate(EvaluationContext context) throws PolicyEvaluationException;
}