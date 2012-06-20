package com.artagon.xacml.v30;

import java.io.Serializable;

import com.artagon.xacml.v30.pdp.ValueExpression;

public interface AttributeExp 
	extends ValueExpression, Serializable
{
	/**
	 * Gets attribute type
	 * 
	 * @return {@Link AttributeValueType}
	 */
	AttributeExpType getType();
	
	/**
	 * Gets attribute expression value
	 * 
	 * @return an attribute expression value
	 */
	Object getValue();
	
	/**
	 * Creates bag with this attribute
	 * in in the bag
	 * 
	 * @return {@link BagOfAttributeExp}
	 */
	BagOfAttributeExp toBag();
	
	/**
	 * Converts this XACML attribute value
	 * to {@link String}
	 * 
	 * @return this attribute value as {@link String}
	 */
	String toXacmlString();
}