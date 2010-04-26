package com.artagon.xacml.v3.policy.spi;

import java.util.Collection;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;

public class RequestAttributesLocator implements AttributeLocator
{
	private Request request;
	
	@Override
	public BagOfAttributeValues<AttributeValue> getAttribute(
			AttributeCategoryId categoryId, 
			String attributeId, 
			RequestAttributeCallback callback) {
	}
	
	@Override
	public Set<AttributeCategoryId> getCategories() {
		return request.getCategories();
	}
	
	@Override
	public Set<String> getProvidedAttributes() {
		return request.getProvidedAttributeIdentifiers();
	}
}
