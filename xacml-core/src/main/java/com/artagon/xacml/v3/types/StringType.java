package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;


public interface StringType extends AttributeValueType
{
	StringValue create(Object v, Object ...params);
	StringValue fromXacmlString(String v, Object ...params);
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
	
	public final class Factory
	{
		private final static StringType INSTANCE = new StringTypeImpl("http://www.w3.org/2001/XMLSchema#string");
		
		public static StringType getInstance(){
			return INSTANCE;
		}
	}
}