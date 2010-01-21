package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;
import com.artagon.xacml.policy.type.Base64BinaryType.Base64BinaryValue;

public interface BooleanType extends AttributeDataType
{	
	BooleanValue create(Object value);
	BooleanValue fromXacmlString(String v);
	
	BagOfAttributesType<BooleanValue> bagOf();
	
	public final class BooleanValue extends BaseAttributeValue<Boolean>
	{
		public BooleanValue(BooleanType type, Boolean value) {
			super(type, value);
		}
	}
}