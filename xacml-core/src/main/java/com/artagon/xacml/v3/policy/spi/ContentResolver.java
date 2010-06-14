package com.artagon.xacml.v3.policy.spi;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface ContentResolver 
{
	Iterable<AttributeCategoryId> getProvidedCategories();
	Node getContent(AttributeCategoryId category, AttributesCallback callback);
}
