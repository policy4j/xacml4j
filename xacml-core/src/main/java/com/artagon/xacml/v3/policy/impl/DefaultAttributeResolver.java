package com.artagon.xacml.v3.policy.impl;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeDesignator;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;

public class DefaultAttributeResolver implements AttributeResolver
{
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		return null;
	}
	
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeCategoryId category, String attributeId,
			AttributeValueType dataType, String issuer) {
		return null;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeCategoryId category, 
			XPath location,
			AttributeValueType dataType) {
		return null;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeDesignator designator) {
		return null;
	}

	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			AttributeSelector selector) {
		return null;
	}
}
