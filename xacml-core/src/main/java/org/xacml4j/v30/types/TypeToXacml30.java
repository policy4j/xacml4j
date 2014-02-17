package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;

public interface TypeToXacml30 extends TypeCapability
{
	AttributeValueType toXacml30(AttributeExp v);
	AttributeExp fromXacml30(AttributeValueType v);
}
