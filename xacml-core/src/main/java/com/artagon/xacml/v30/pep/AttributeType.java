package com.artagon.xacml.v30.pep;


public interface AttributeType 
{
	String getDataTypeId();
	Attribute create(Object value, Object ...params);
}
