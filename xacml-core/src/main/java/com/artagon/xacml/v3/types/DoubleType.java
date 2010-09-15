package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface DoubleType extends AttributeValueType
{
	DoubleValue create(Object v, Object ...params);
	DoubleValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<DoubleValue> bagOf();
	
	final class DoubleValue extends BaseAttributeValue<Double>
	{
		public DoubleValue(DoubleType type, Double value) {
			super(type, value);
		}
	}
	
	public final class Factory
	{
		private final static DoubleType INSTANCE = new DoubleTypeImpl("http://www.w3.org/2001/XMLSchema#double");
		
		public static DoubleType getInstance(){
			return INSTANCE;
		}
	}
}