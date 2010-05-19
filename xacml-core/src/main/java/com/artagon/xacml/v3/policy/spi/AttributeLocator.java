package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.AttributeDesignator;

public interface AttributeLocator 
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
	
	BagOfAttributeValues<AttributeValue> resolve(AttributeDesignator ref, Request request);
}
