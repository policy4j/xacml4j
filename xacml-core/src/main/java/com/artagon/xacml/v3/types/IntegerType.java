package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface IntegerType extends AttributeValueType
{
	IntegerValue create(Object value, Object ...params);
	IntegerValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<IntegerValue> bagOf();
	
	final class IntegerValue extends BaseAttributeValue<Long>
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
		
		public static BagOfAttributeValues<IntegerValue> bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<IntegerValue> bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<IntegerValue> emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}