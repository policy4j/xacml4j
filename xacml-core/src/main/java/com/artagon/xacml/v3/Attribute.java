package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;

public interface Attribute {

	/**
	 * Gets attribute identifier.
	 * 
	 * @return attribute identifier
	 */
	String getAttributeId();

	/**
	 * Gets attribute values as collection of
	 * {@link AttributeValue} instances
	 * 
	 * @return collection of {@link AttributeValue} 
	 * instances
	 */
	Collection<AttributeValue> getValues();

	/**
	 * Gets this attribute issuer
	 * 
	 * @return issuer of this attribute
	 * identifier or <code>null</code>
	 * if it's not available
	 */
	String getIssuer();

	/**
	 * Tests if this attribute needs
	 * to be included back to the
	 * evaluation result
	 * 
	 * @return <code>true</code>
	 * if needs to be included
	 */
	boolean isIncludeInResult();

	Collection<AttributeValue> getValuesByType(AttributeValueType type);

}