package com.artagon.xacml.v3.policy;



public interface AttributeValue extends Value
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
	AttributeValue evaluate(EvaluationContext context) throws EvaluationException;
}