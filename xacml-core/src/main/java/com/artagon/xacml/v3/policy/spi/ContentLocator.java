package com.artagon.xacml.v3.policy.spi;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCallback;
import com.artagon.xacml.v3.AttributeCategoryId;

public interface ContentLocator 
{
	Iterable<AttributeCategoryId> getProvidedCategories();
	Node getContent(AttributeCategoryId category, AttributeCallback callback);
}
