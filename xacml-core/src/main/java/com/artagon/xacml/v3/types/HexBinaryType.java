package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface HexBinaryType  extends AttributeValueType
{
	HexBinaryValue create(Object any, Object ...params);
	HexBinaryValue fromXacmlString(String v, Object ...params);
	BagOfAttributeValuesType<HexBinaryValue> bagOf();
	
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
	}
}
