package com.artagon.xacml.v3.types;

public final class Base64BinaryValue extends BaseAttributeValue<BinaryValue>
{
	public Base64BinaryValue(BinaryValue value) {
		super(Base64BinaryType.BASE64BINARY, value);
	}
	
	@Override
	public String toXacmlString() {
		return getValue().toBase64();
	}
}

