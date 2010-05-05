package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Node;

public interface IndividualRequest 
{
	boolean isReturnPolicyIdList();
	Collection<Attributes> getAttributes();
	Iterable<AttributeCategoryId> getCategories();
	Attributes getAttributes(AttributeCategoryId category);
	Node getContent(AttributeCategoryId category);
	Collection<Attributes> getIncludeInResultAttributes();
}
