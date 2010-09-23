package com.artagon.xacml.v3;

import java.util.Collection;


public interface AttributeValueType  extends ValueType
{
	/**
	 * Gets data type identifier
	 * 
	 * @return data type identifier
	 */
	String getDataTypeId();
	
	/**
	 * Tests if a given {@link Object} instance
	 * can be converted to the instance of {@link AttributeValue}
	 * of this data type
	 * 
	 * @param any an {@link Object} instance
	 * @return <code>true</code> if given instance
	 * can be converted to {@link AttributeValue} instance
	 * of this data type
	 */
	boolean isConvertableFrom(Object any);
	
	/**
	 * Parses given XACML string representation of an attribute
	 * value to an actual {@link BaseAttribute} of this type
	 * 
	 * @param v an XACML string value representation of this type
	 * @param params an additional parameters used to create
	 * value of this type from given string
	 * @return {@link BaseAttribute} instance of this type
	 * @exception IllegalArgumentException if given value does not
	 * represent value of this type
	 */
	AttributeValue fromXacmlString(String v, Object ...params);

	/**
	 * Creates an attribute from a given object.
	 * Object can be instance of {@link this#getValueClazz()}
	 * or can be instance of {@link String}
	 * 
	 * @param object an object
	 * @param params  an additional parameters used to
	 * create a value of this type
	 * @return {@link AttributeValue}
	 * @exception IllegalArgumentException if attribute value of 
	 * this type can not be created from a given value
	 * and parameters
	 */
	AttributeValue create(Object object, Object ...params);
	
	/**
	 * Creates type representing collection of 
	 * attribute values of this
	 * data type
	 * 
	 * @return {@link BagOfAttributeValuesType} instance
	 */
	BagOfAttributeValuesType bagType();
	
	BagOfAttributeValues bagOf(AttributeValue ...values);
	BagOfAttributeValues bagOf(Collection<AttributeValue> values);
	BagOfAttributeValues emptyBag();
}