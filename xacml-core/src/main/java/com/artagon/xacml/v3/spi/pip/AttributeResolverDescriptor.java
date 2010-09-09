package com.artagon.xacml.v3.spi.pip;

import java.util.Map;
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
	AttributeDescriptor getAttributeDescriptor(AttributeCategoryId categoryId, String attributeId);
	
	Set<String> getProvidedAttributeIds(AttributeCategoryId category);
	
	/**
	 * Gets attributes of the given category
	 * 
	 * @param category an attribute category
	 * @return a map by the attribute id
	 */
	Map<String, AttributeDescriptor> getAttributes(AttributeCategoryId category);
	
	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 * 
	 * @param attributeId
	 * @return
	 */
	boolean isAttributeProvided(AttributeCategoryId category, String attributeId);
}
