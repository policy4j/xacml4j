package com.artagon.xacml.v3.types;

import java.net.URI;

import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;

public interface AnyURIType extends AttributeValueType
{	
	AnyURIValue create(Object value, Object ...params);
	
	AnyURIValue fromXacmlString(String v, Object ...params);
	
	BagOfAttributeValuesType<AnyURIValue> bagOf();
	
	final class AnyURIValue extends BaseAttributeValue<URI> 
	{
		public AnyURIValue(AnyURIType type, URI value) {
			super(type, value);
		}
	}
}