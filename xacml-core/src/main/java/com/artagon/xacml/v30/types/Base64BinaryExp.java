package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.core.BinaryValue;

public final class Base64BinaryExp extends BaseAttributeExp<BinaryValue>
{
	private static final long serialVersionUID = 3298986540546649848L;

	Base64BinaryExp(BinaryValue value) {
		super(Base64BinaryType.BASE64BINARY, value);
	}
	
	@Override
	public String toXacmlString() {
		return getValue().toBase64Encoded();
	}
}

