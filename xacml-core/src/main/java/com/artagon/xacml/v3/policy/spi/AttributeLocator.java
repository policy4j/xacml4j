package com.artagon.xacml.v3.policy.spi;

import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;

public interface AttributeLocator 
{
	/**
	 * Gets an attribute categories supported
	 * by this locator
	 * 
	 * @return a set of attribute categories
	 */
	Set<AttributeCategoryId> getCategories();
	
	/**
	 * Gets an attribute identifier provided
	 * by this locator
	 * 
	 * @return an set of attribute identifiers
	 * provided by this locator
	 */
	Set<String> getProvidedAttributes();
	
	/**
	 * Resolves an attribute by given identifier
	 * 
	 * @param attributeId an attribute identifier
	 * @param callback a callback to get request 
	 * context attributes
	 * 
	 * @return {@link BagOfAttributeValues}
	 */
	BagOfAttributeValues<AttributeValue> getAttribute(
			String attributeId, ContextAttributeCallback callback);
}
