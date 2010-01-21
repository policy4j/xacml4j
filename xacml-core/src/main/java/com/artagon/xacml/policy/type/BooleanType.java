package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface BooleanType extends AttributeDataType
{	
	BooleanValue create(Object value);
	BooleanValue fromXacmlString(String v);
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
}