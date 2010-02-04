package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.AttributeCategoryId;

interface AttributeReference extends Expression 
{
	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 * 
	 * @return {@link AttributeValueType}
	 */
	AttributeValueType getDataType();

	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	AttributeCategoryId getCategory();

}