package org.xacml4j.v30.types;

import org.xacml4j.v30.AttributeExp;

public interface TypeToString extends TypeCapability 
{
	String toString(AttributeExp exp);
	AttributeExp fromString(String v);
}
