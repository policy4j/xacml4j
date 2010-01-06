package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface DoubleType extends AttributeDataType
{
	DoubleValue create(Object v);
	DoubleValue fromXacmlString(String v);
	
	final class DoubleValue extends BaseAttributeValue<Double>
	{
		public DoubleValue(DoubleType type, Double value) {
			super(type, value);
		}
	}
}