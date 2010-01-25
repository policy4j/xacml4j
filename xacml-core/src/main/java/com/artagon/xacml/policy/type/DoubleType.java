package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;

public interface DoubleType extends AttributeType
{
	DoubleValue create(Object v);
	DoubleValue fromXacmlString(String v);
	
	BagOfAttributesType<DoubleValue> bagOf();
	
	final class DoubleValue extends BaseAttributeValue<Double>
	{
		public DoubleValue(DoubleType type, Double value) {
			super(type, value);
		}
	}
}