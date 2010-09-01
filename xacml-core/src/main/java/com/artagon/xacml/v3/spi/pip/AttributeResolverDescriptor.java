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
	 * Gets category of attributes
	 * 
	 * @return {@link AttributeCategoryId}
	 */
	AttributeCategoryId getCategory();

	/**
	 * Gets attribute identifiers provided by this resolver
	 * 
	 * @param categoryId an attribute category
	 * @return a {@link Set} with all attribute identifiers
	 */
	Set<String> getProvidedAttributeIds();
	
	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 * 
	 * @param attributeId
	 * @return
	 */
	boolean isAttributeProvided(String attributeId);
}
