package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.BagOfAttributeValuesType;

public interface DoubleType extends AttributeValueType
{
	DoubleValue create(Object v);
	DoubleValue fromXacmlString(String v);
	
	BagOfAttributeValuesType<DoubleValue> bagOf();
	
	final class DoubleValue extends BaseAttributeValue<Double>
	{
		public DoubleValue(DoubleType type, Double value) {
			super(type, value);
		}
	}
}