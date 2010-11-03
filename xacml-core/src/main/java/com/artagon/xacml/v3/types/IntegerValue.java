package com.artagon.xacml.v3.types;

public final class IntegerValue extends SimpleAttributeValue<Long>
{
	private static final long serialVersionUID = 6654857010399020496L;

	IntegerValue(IntegerType type, Long value) {
		super(type, value);
	}
	
}

