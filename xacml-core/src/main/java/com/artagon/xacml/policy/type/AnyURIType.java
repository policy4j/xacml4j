package com.artagon.xacml.policy.type;

import java.net.URI;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.BagOfAttributeValuesType;

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