package com.artagon.xacml.v30.policy.type;

import com.artagon.xacml.v30.policy.AttributeValueType;
import com.artagon.xacml.v30.policy.BagOfAttributeValuesType;

public interface IntegerType extends AttributeValueType
{
	IntegerValue create(Object value);
	IntegerValue fromXacmlString(String v);
	BagOfAttributeValuesType<IntegerValue> bagOf();
	
	final class IntegerValue extends BaseAttributeValue<Long>
	{
		public IntegerValue(IntegerType type, Long value) {
			super(type, value);
		}
		
	}
}