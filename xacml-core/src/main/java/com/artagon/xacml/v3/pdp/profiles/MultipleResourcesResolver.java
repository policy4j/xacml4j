package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Set;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.RequestContextAttributesCallback;

public interface MultipleResourcesResolver 
{
	/**
	 * Gets supported resource identifiers
	 * 
	 * @return a {@link Set} of {@link AttributeValue}
	 * instances representing supported resource
	 * identifiers
	 */
	Set<AttributeValue> getSupportedResourceIds();
	
	/**
	 * Resolves immediate children resources of 
	 * the given resource
	 * 
	 * @param resource a resource identifier
	 * @param callback a request context attribute callback
	 * @return a collection of immediate children of the given resource
	 */
	Collection<AttributeValue> resolveChildrenResources(AttributeValue resource, 
			RequestContextAttributesCallback callback);
	
	Collection<AttributeValue> resolveDescendantResources(AttributeValue resource, 
			RequestContextAttributesCallback callback);
}
