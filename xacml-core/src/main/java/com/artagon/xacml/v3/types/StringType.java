package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;


public interface StringType extends AttributeValueType
{
	StringValue create(Object v, Object ...params);
	StringValue fromXacmlString(String v, Object ...params);
	
	final class StringValue extends SimpleAttributeValue<String>
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
		
		public static StringValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static StringValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagType().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagType().createEmpty();
		}
	}
}