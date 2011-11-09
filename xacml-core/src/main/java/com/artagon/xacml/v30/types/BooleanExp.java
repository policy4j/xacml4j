package com.artagon.xacml.v30.types;

public final class BooleanExp extends 
	BaseAttributeExp<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	BooleanExp(BooleanType type, Boolean value) {
		super(type, value);
	}
}
