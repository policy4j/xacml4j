package com.artagon.xacml.v3.policy;

public interface AttributeDesignator extends AttributeRefExpression
{
	/**
	 * Gets attribute identifier
	 * in the request context
	 * 
	 * @return attribute identifier
	 * in the request context
	 */
	String getAttributeId();

	/**
	 * Gets attribute issuer.
	 * 
	 * @return attribute issuer
	 * or <code>null</code> if issuer
	 * is not specified
	 */
	String getIssuer();
}