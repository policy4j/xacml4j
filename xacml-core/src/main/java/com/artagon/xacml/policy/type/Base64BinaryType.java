package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.BagOfAttributeValuesType;

public interface Base64BinaryType extends AttributeValueType
{	
	Base64BinaryValue create(Object any);
	Base64BinaryValue fromXacmlString(String v);
	BagOfAttributeValuesType<Base64BinaryValue> bagOf();
	
	final class Base64BinaryValue extends BaseAttributeValue<BinaryValue>
	{
		public Base64BinaryValue(Base64BinaryType type, BinaryValue value) {
			super(type, value);
		}
	
		@Override
		public String toXacmlString() {
			return getValue().toBase64();
		}
	}
}
