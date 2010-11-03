package com.artagon.xacml.v3.types;

public final class HexBinaryValue extends SimpleAttributeValue<BinaryValue>
{
	private static final long serialVersionUID = 8087916652227967791L;

	HexBinaryValue(HexBinaryType type, BinaryValue value) {
		super(type, value);
	}

	@Override
	public String toXacmlString() {
		return getValue().toHex();
	}
}

