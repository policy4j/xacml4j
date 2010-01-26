package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

public interface HexBinaryType  extends AttributeValueType
{
	HexBinaryValue create(Object any);
	HexBinaryValue fromXacmlString(String v);
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
