package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;

public interface AttributeAssignment extends PolicyElement
{

	/**
	 * Gets attribute identifier
	 * 
	 * @return attribute identifier
	 */
	String getAttributeId();

	/**
	 * Gets attribute value
	 * 
	 * @return attribute value
	 */
	AttributeValue getAttribute();

	/**
	 * Gets attribute category
	 * 
	 * @return attribute category
	 */
	AttributeCategoryId getCategory();

	/**
	 * Gets attribute issuer identifier
	 * 
	 * @return attribute issuer
	 */
	String getIssuer();

}