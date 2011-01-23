package com.artagon.xacml.v30.types;

public final class BooleanValue extends SimpleAttributeValue<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	BooleanValue(BooleanType type, Boolean value) {
		super(type, value);
	}
}
