package org.xacml4j.v30.types;

import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;

public interface TypeToXacml30 extends TypeCapability
{
	AttributeValueType toXacml30(Types types, AttributeExp v);
	AttributeExp fromXacml30(Types types, AttributeValueType v);
}
