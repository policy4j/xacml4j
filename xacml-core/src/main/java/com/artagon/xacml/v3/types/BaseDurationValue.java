package com.artagon.xacml.v3.types;

import javax.xml.datatype.Duration;

import com.artagon.xacml.v3.AttributeValueType;

class BaseDurationValue  extends SimpleAttributeValue<Duration>
{
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
