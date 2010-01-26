package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;


public interface StringType extends AttributeValueType
{
	StringValue create(Object v);
	StringValue fromXacmlString(String v);
	BagOfAttributeValuesType<StringValue> bagOf();
	
	final class StringValue extends BaseAttributeValue<String>
	{
		public StringValue(StringType type, String value) {
			super(type, value);
		}
		
		public boolean equalsIgnoreCase(StringValue v){
			return getValue().equalsIgnoreCase(v.getValue());
		}
	}
}