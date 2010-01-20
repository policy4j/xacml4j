package com.artagon.xacml.policy.type;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface BooleanType extends AttributeDataType
{	
	BooleanValue create(Object value);
	BooleanValue fromXacmlString(String v);
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
	
	public enum BooleanTypeId implements DataTypeId
	{
		BOOLEAN("http://www.w3.org/2001/XMLSchema#boolean");
		
		private String typeId;
		
		private BooleanTypeId(String typeId){
			this.typeId = typeId;
		}
		
		public String toString(){
			return typeId;
		}
	}
}