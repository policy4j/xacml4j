package com.artagon.xacml.v30.types;

public final class StringValueExp extends BaseAttributeExpression<String>
{
	private static final long serialVersionUID = 657672949137533611L;

	StringValueExp(StringType type, String value) {
		super(type, value);
	}
	
	public boolean equalsIgnoreCase(StringValueExp v){
		return getValue().equalsIgnoreCase(v.getValue());
	}
}

