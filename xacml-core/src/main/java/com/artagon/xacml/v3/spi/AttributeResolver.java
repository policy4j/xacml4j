package com.artagon.xacml.v3.spi;

import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface AttributeResolver 
{
	/**
	 * Optional attribute issuing 
	 * authority identifier
	 * 
	 * @return attribute issuing authority
	 * identifier or <code>null</code>
	 */
	String getIssuer();
	
	/**
	 * Gets an attribute categories supported
	 * by this locator
	 * 
	 * @return a set of attribute categories
	 */
	Set<AttributeCategoryId> getProvidedCategories();
	
	/**
	 * Gets an attribute identifiers provided
	 * by this resolver
	 * 
	 * @return an set of attribute identifiers
	 * provided by this locator
	 */
	Set<String> getProvidedAttributes();
	
	BagOfAttributeValues<? extends AttributeValue> resolve(AttributeDesignator ref, 
			AttributesCallback callback);
}
