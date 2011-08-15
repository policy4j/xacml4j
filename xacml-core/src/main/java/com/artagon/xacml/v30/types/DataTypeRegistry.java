package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeValueType;

public interface DataTypeRegistry 
{
	/**
	 * Gets type by given type identifier
	 * 
	 * @param typeId a type identifier
	 * @return {@link AttributeValueType} a data type instance
	 */
	AttributeValueType getType(String typeId);
	
//	AttributeValue create(String typeId, Object value);
//	AttributeValue create(String typeId,Object value, Object... params);
//	AttributeValue create(String typeId, Object value, Map<QName, String> params); 
}
