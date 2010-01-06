package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;


public interface StringType extends AttributeDataType
{
	StringValue create(Object v);
	StringValue fromXacmlString(String v);
	
	final class StringValue extends BaseAttributeValue<String>
	{
		public StringValue(StringType type, String value) {
			super(type, value);
		}
	}
}