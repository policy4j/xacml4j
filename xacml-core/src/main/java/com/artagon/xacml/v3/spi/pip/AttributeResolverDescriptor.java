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
	
	boolean isCategorySupported(AttributeCategoryId category);
	
	Set<AttributeCategoryId> getSupportedCategores();
	
	/**
	 * Gets attribute of the given category
	 * with a given identifier descriptor
	 * 
	 * @param attributeId an attribute identifier
	 * @return {@link AttributeDescriptor}
	 */
	AttributeDescriptor getAttributeDescriptor(String attributeId);
	
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
