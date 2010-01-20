package com.artagon.xacml.policy.type;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface Base64BinaryType extends AttributeDataType
{	
	Base64BinaryValue create(Object any);
	Base64BinaryValue fromXacmlString(String v);
	
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
	
	public enum Base64BinaryTypeId implements DataTypeId
	{
		BASE54BINARY("http://www.w3.org/2001/XMLSchema#base64Binary");
		
		private String typeId;
		
		private Base64BinaryTypeId(String typeId){
			this.typeId = typeId;
		}
		
		public String toString(){
			return typeId;
		}
	}
}
