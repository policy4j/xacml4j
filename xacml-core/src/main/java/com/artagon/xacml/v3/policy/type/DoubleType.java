package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeValue;

public interface DoubleType extends AttributeValueType
{
	DoubleValue create(Object v, Object ...params);
	DoubleValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<DoubleValue> bagOf();
	
	final class DoubleValue extends BaseAttributeValue<Double>
	{
		public DoubleValue(DoubleType type, Double value) {
			super(type, value);
		}
	}
}