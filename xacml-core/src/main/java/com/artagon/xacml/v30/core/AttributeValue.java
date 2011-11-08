package com.artagon.xacml.v30.core;

public interface AttributeValue 
{
	Object getValue();
	AttributeValueType getType();
	String toXacmlString();
}
