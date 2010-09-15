package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface Base64BinaryType extends AttributeValueType
{	
	Base64BinaryValue create(Object any, Object ...params);
	Base64BinaryValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<Base64BinaryValue> bagOf();
	
	final class Base64BinaryValue extends BaseAttributeValue<BinaryValue>
	{
		public Base64BinaryValue(Base64BinaryType type, BinaryValue value) {
			super(type, value);
		}
		
		@Override
		public String toXacmlString() {
			return getValue().toBase64();
		}
	}
	
	public final class Factory
	{
		private final static Base64BinaryType INSTANCE = new Base64BinaryTypeImpl("http://www.w3.org/2001/XMLSchema#base64Binary");
		
		public static Base64BinaryType getInstance(){
			return INSTANCE;
		}
	}
}
