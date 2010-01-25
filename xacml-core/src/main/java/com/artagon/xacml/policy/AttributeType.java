package com.artagon.xacml.policy;


public interface AttributeType extends ValueType
{
	/**
	 * Gets attribute value java class
	 * 
	 * @return {@link Class}
	 */
	Class<?> getValueClass();
	
	/**
	 * Gets data type identifier
	 * 
	 * @return data type identifier
	 */
	String getDataTypeId();
	
	/**
	 * Tests if a given {@link Object} instance
	 * can be converted to the instance of {@link Attribute}
	 * of this data type
	 * 
	 * @param any an {@link Object} instance
	 * @return <code>true</code> if given instance
	 * can be converted to {@link Attribute} instance
	 * of this data type
	 */
	boolean isConvertableFrom(Object any);
	
	/**
	 * Parses given XACML string representation of an attribute
	 * value to an actual {@link BaseAttribute} of this type
	 * 
	 * @param v an XACML string value representation of this type
	 * @return {@link BaseAttribute} instance of this type
	 * @exception IllegalArgumentException if given value does not
	 * represent value of this type
	 */
	Attribute fromXacmlString(String v);
	
	/**
	 * Creates an attribute from a given object.
	 * Object can be instance of {@link this#getValueClazz()}
	 * or can be instance of {@link String}
	 * 
	 * @param object an object
	 * @return {@link Attribute}
	 */
	Attribute create(Object object);
	
	/**
	 * Creates type representing collection of 
	 * attribute values of this
	 * data type
	 * 
	 * @return {@link BagOfAttributesType} instance
	 */
	BagOfAttributesType<?> bagOf();
}