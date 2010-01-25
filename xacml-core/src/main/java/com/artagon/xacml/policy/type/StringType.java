package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;


public interface StringType extends AttributeType
{
	StringValue create(Object v);
	StringValue fromXacmlString(String v);
	BagOfAttributesType<StringValue> bagOf();
	
	final class StringValue extends BaseAttributeValue<String>
	{
		public StringValue(StringType type, String value) {
			super(type, value);
		}
	}
}