package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;

public interface HexBinaryType  extends AttributeType
{
	HexBinaryValue create(Object any);
	HexBinaryValue fromXacmlString(String v);
	BagOfAttributesType<HexBinaryValue> bagOf();
	
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
