package com.artagon.xacml.v3.types;


public final class StringValue extends SimpleAttributeValue<String>
{
	private static final long serialVersionUID = 657672949137533611L;

	StringValue(StringType type, String value) {
		super(type, value);
	}
	
	public boolean equalsIgnoreCase(StringValue v){
		return getValue().equalsIgnoreCase(v.getValue());
	}
}

