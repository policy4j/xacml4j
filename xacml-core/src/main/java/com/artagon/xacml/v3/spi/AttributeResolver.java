package com.artagon.xacml.v3.spi;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface AttributeResolver 
{
	/**
	 * Gets an attribute categories supported
	 * by this locator
	 * 
	 * @return a set of attribute categories
	 */
	Iterable<AttributeCategoryId> getProvidedCategories();
	
	/**
	 * Gets an attribute identifiers provided
	 * by this locator
	 * 
	 * @return an set of attribute identifiers
	 * provided by this locator
	 */
	Iterable<String> getProvidedAttributes();
	
	BagOfAttributeValues<AttributeValue> resolve(AttributeDesignator ref, 
			AttributesCallback callback);
}
