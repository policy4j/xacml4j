package com.artagon.xacml.v3.types;


public final class StringValue extends SimpleAttributeValue<String>
{
	StringValue(StringType type, String value) {
		super(type, value);
	}
	
	public boolean equalsIgnoreCase(StringValue v){
		return getValue().equalsIgnoreCase(v.getValue());
	}
}

