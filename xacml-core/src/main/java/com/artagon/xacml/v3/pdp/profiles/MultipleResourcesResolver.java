package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.RequestContextAttributesCallback;

public interface MultipleResourcesResolver 
{
	Collection<AttributeValue> resolveChildrenResources(AttributeValue resource, 
			RequestContextAttributesCallback callback);
	
	Collection<AttributeValue> resolveDescendantResources(AttributeValue resource, 
			RequestContextAttributesCallback callback);
}
