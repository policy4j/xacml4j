package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

public interface BooleanType extends AttributeValueType
{	
	BooleanValue create(Object value, Object ...params);
	BooleanValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<BooleanValue> bagOf();
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
}