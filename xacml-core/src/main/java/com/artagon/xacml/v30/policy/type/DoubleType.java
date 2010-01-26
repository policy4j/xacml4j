package com.artagon.xacml.v30.policy.type;

import com.artagon.xacml.v30.policy.AttributeValueType;
import com.artagon.xacml.v30.policy.BagOfAttributeValuesType;

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