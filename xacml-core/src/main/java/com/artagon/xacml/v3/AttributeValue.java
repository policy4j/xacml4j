package com.artagon.xacml.v3;

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
	
//	/**
//	 * Serializes this attribute value
//	 * to compact binary representation
//	 * 
//	 * @param buf a buffer
//	 * @param pos a start position
//	 * @return length in bytes for this value in a given buffer
//	 */
//	int toXacmlBinary(byte[] buf, int pos);
//	
//	/**
//	 * Serializes this attribute value
//	 * to compact binary representation
//	 * 
//	 * @param buf a buffer
//	 * @param pos a start position
//	 * @return length in bytes for this value in a given buffer
//	 */
//	int toXacmlBinary(ByteBuffer buf);
}