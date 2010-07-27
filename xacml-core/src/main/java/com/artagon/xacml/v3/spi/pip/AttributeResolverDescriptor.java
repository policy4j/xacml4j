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
	 * Gets attribute identifiers provided by this resolver
	 * 
	 * @param categoryId an attribute category
	 * @return a {@link Set} with all attribute identifiers
	 */
	Set<String> getProvidedAttributes(AttributeCategoryId categoryId);
	
	boolean isAttributeProvided(AttributeCategoryId categoryId, String attributeId);
}
