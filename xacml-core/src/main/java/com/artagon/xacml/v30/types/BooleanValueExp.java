package com.artagon.xacml.v30.types;

public final class BooleanValueExp extends 
	BaseAttributeExpression<Boolean>
{
	private static final long serialVersionUID = -421397689674188254L;

	BooleanValueExp(BooleanType type, Boolean value) {
		super(type, value);
	}
}
