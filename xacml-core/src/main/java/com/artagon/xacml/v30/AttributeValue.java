package com.artagon.xacml.v30;

import java.io.Serializable;

public interface AttributeValue 
	extends ValueExpression, Serializable
{
	/**
	 * Gets attribute type
	 * 
	 * @return {@Link AttributeValueType}
	 */
	AttributeValueType getType();
	
	/**
	 * Converts this XACML attribute value
	 * to {@link String}
	 * 
	 * @return this attribute value as {@link String}
	 */
	String toXacmlString();
}