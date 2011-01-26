package com.artagon.xacml.v30.types;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeValueType;

public interface DataTypeRegistry 
{
	AttributeValueType getType(String typeId);
	void setTypes(Collection<AttributeValueType> types);
}
