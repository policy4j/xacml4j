package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValueType;
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
	}
}