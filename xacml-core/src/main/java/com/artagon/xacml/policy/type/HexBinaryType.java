package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface HexBinaryType  extends AttributeDataType
{
	HexBinaryValue create(Object any);
	HexBinaryValue fromXacmlString(String v);
	
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
