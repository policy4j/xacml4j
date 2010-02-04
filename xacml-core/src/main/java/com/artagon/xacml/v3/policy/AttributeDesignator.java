package com.artagon.xacml.v3.policy;

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

	/**
	 * Governs whether this designator evaluates 
	 * to an empty bag or {@link EvaluationIndeterminateException} 
	 * exception is thrown
	 * 
	 * @return
	 */
	boolean isMustBePresent();
}