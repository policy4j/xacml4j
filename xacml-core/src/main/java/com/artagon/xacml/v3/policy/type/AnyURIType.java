package com.artagon.xacml.v3.policy.type;

import java.net.URI;

import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

public interface AnyURIType extends AttributeValueType
{	
	AnyURIValue create(Object value);
	AnyURIValue fromXacmlString(String v);
	BagOfAttributeValuesType<AnyURIValue> bagOf();
	
	final class AnyURIValue extends BaseAttributeValue<URI> 
	{
		public AnyURIValue(AnyURIType type, URI value) {
			super(type, value);
		}
	}
}