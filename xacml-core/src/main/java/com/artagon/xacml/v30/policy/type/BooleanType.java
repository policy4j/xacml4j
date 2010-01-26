package com.artagon.xacml.v30.policy.type;

import com.artagon.xacml.v30.policy.AttributeValueType;
import com.artagon.xacml.v30.policy.BagOfAttributeValuesType;

public interface BooleanType extends AttributeValueType
{	
	BooleanValue create(Object value);
	BooleanValue fromXacmlString(String v);
	
	BagOfAttributeValuesType<BooleanValue> bagOf();
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
}