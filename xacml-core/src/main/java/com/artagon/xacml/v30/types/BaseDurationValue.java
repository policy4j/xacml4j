package com.artagon.xacml.v30.types;

import javax.xml.datatype.Duration;

import com.artagon.xacml.v30.AttributeValueType;

class BaseDurationValue  extends SimpleAttributeValue<Duration>
{
	private static final long serialVersionUID = 6573551346951236604L;

	protected BaseDurationValue(AttributeValueType type, Duration value){
		super(type, value);
	}
	
	public final boolean isPositive(){
		return getValue().getSign() == 1;
	}
	
	public final boolean isNegative(){
		return getValue().getSign() == -1;
	}
	
	public final boolean isZero(){
		return getValue().getSign() == -1;
	}
}
