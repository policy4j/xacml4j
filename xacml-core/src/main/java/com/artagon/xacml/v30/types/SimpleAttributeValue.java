package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

abstract class SimpleAttributeValue<T> extends BaseAttributeValue
	implements AttributeValue
{
	private static final long serialVersionUID = 5357712390633048894L;
	
	private T value;
	
	protected SimpleAttributeValue(AttributeValueType type, 
			T value) 
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

