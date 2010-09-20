package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface BooleanType extends AttributeValueType
{	
	BooleanValue create(Object value, Object ...params);
	BooleanValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<BooleanValue> bagOf();
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
	
	public final class Factory
	{
		private final static BooleanType INSTANCE = new BooleanTypeImpl("http://www.w3.org/2001/XMLSchema#boolean");
		
		public static BooleanType getInstance(){
			return INSTANCE;
		}
		
		public static BooleanValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static BooleanValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues<BooleanValue> bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<BooleanValue> bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues<BooleanValue> emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}