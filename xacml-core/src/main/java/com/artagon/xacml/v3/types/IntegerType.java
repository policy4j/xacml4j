package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface IntegerType extends AttributeValueType
{
	IntegerValue create(Object value, Object ...params);
	IntegerValue fromXacmlString(String v, Object ...params);
	
	final class IntegerValue extends SimpleAttributeValue<Long>
	{
		public IntegerValue(IntegerType type, Long value) {
			super(type, value);
		}
		
	}
	
	public final class Factory
	{
		private final static IntegerType INSTANCE = new IntegerTypeImpl("http://www.w3.org/2001/XMLSchema#integer");
		
		public static IntegerType getInstance(){
			return INSTANCE;
		}
		
		public static IntegerValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static IntegerValue fromXacmlString(String v, Object ...params){
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