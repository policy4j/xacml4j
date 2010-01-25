package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface IntegerType extends AttributeType
{
	IntegerValue create(Object value);
	IntegerValue fromXacmlString(String v);
	BagOfAttributesType<IntegerValue> bagOf();
	
	final class IntegerValue extends BaseAttributeValue<Long>
	{
		public IntegerValue(IntegerType type, Long value) {
			super(type, value);
		}
		
	}
}