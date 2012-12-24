package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.Set;

import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.pdp.RequestContextCallback;


public interface MultipleResourcesResolver 
{
	/**
	 * Gets supported resource identifiers
	 * 
	 * @return a {@link Set} of {@link AttributeExp}
	 * instances representing supported resource
	 * identifiers
	 */
	Set<AttributeExp> getSupportedResourceIds();
	
	/**
	 * Resolves immediate children resources of 
	 * the given resource
	 * 
	 * @param resource a resource identifier
	 * @param callback a request context attribute callback
	 * @return a collection of immediate children of the given resource
	 */
	Collection<AttributeExp> resolveChildrenResources(
			AttributeExp resource, 
			RequestContextCallback callback);
	
	Collection<AttributeExp> resolveDescendantResources(
			AttributeExp resource, 
			RequestContextCallback callback);
}
