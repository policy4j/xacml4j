package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;
import com.artagon.xacml.v3.policy.spi.type.BaseAttributeValue;

public interface HexBinaryType  extends AttributeValueType
{
	HexBinaryValue create(Object any, Object ...params);
	HexBinaryValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<HexBinaryValue> bagOf();
	
	final class HexBinaryValue extends BaseAttributeValue<BinaryValue>
	{
		public HexBinaryValue(HexBinaryType type, BinaryValue value) {
			super(type, value);
		}
	
		@Override
		public String toXacmlString() {
			return getValue().toHex();
		}
		
	}
}
