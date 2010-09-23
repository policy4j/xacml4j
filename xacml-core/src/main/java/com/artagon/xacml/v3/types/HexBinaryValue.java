package com.artagon.xacml.v3.types;

public final class HexBinaryValue extends SimpleAttributeValue<BinaryValue>
{
	HexBinaryValue(HexBinaryType type, BinaryValue value) {
		super(type, value);
	}

	@Override
	public String toXacmlString() {
		return getValue().toHex();
	}
}

