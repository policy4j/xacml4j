package com.artagon.xacml.v30.pep;

public interface Attribute 
{
	AttributeType getType();
	String toXacmlString();
}
