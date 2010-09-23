package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

abstract class SimpleAttributeValue<T> extends BaseAttributeValue
	implements AttributeValue
{
	private T value;
	
	protected SimpleAttributeValue(AttributeValueType type, T value) 
	{
		super(type);
		Preconditions.checkNotNull(value);
		this.value = value;
	}
	
	public final T getValue(){
		return value;
	}
		
	
	@Override
	public String toXacmlString() {
		return value.toString();
	}
		
	@Override
	public String toString() {
		return Objects.toStringHelper(this).
		add("Value", value).
		add("Type", getType()).toString();
	}
}

