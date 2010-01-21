package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;
import com.artagon.xacml.policy.type.DateTimeType.DateTimeValue;

public interface HexBinaryType  extends AttributeDataType
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
