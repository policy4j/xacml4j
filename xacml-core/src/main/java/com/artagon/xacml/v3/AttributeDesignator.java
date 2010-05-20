package com.artagon.xacml.v3;

public interface AttributeDesignator extends AttributeReference
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