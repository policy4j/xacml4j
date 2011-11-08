package com.artagon.xacml.v30.core;


public interface AttributeValueType 
{
	String getDataTypeId();
	AttributeValue create(Object value, Object ...params);
}
