package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;

public interface BooleanType extends AttributeType
{	
	BooleanValue create(Object value);
	BooleanValue fromXacmlString(String v);
	
	BagOfAttributesType<BooleanValue> bagOf();
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
}