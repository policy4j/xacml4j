package com.artagon.xacml.v3.spi.pip;

import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface AttributeResolverDescriptor 
{
	/**
	 * Gets resolver identifier
	 * 
	 * @return a resolver identifier
	 */
	String getIssuer();
	
	/**
	 * Gets provided attribute categories
	 *  
	 * @return a set of provided {@link AttributeCategoryId} instances
	 */
	Set<AttributeCategoryId> getProvidedCategories();
	
	/**
	 * Gets provided attribute identifiers
	 * 
	 * @return a set of attribute identifiers
	 */
	Set<String> getProvidedAttributes();
}
