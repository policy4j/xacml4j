package com.artagon.xacml.policy.type;

import java.net.URI;

import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.policy.BaseAttributeValue;

public interface AnyURIType extends AttributeType
{	
	AnyURIValue create(Object value);
	AnyURIValue fromXacmlString(String v);
	BagOfAttributesType<AnyURIValue> bagOf();
	
	final class AnyURIValue extends BaseAttributeValue<URI> 
	{
		public AnyURIValue(AnyURIType type, URI value) {
			super(type, value);
		}
	}
}