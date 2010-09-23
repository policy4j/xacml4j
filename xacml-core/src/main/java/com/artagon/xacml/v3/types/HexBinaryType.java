package com.artagon.xacml.v3.types;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface HexBinaryType  extends AttributeValueType
{
	HexBinaryValue create(Object any, Object ...params);
	HexBinaryValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType bagOf();
	
	final class HexBinaryValue extends BaseAttributeValue<BinaryValue>
	{
		public HexBinaryValue(HexBinaryType type, BinaryValue value) {
			super(type, value);
		}
	
		@Override
		public String toXacmlString() {
			return getValue().toHex();
		}
	}
	
	public final class Factory
	{
		private final static HexBinaryType INSTANCE = new HexBinaryTypeImpl("http://www.w3.org/2001/XMLSchema#hexBinary");
		
		public static HexBinaryType getInstance(){
			return INSTANCE;
		}
		
		public static HexBinaryValue create(Object v, Object ...params){
			return INSTANCE.create(v, params);
		}
		
		public static HexBinaryValue fromXacmlString(String v, Object ...params){
			return INSTANCE.fromXacmlString(v, params);
		}
		
		public static BagOfAttributeValues bagOf(AttributeValue ...values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues bagOf(Collection<AttributeValue> values){
			return INSTANCE.bagOf().create(values);
		}
		
		public static BagOfAttributeValues emptyBag(){
			return INSTANCE.bagOf().createEmpty();
		}
	}
}
