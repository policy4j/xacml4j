package com.artagon.xacml.v3.spi.pip;

import java.util.Set;

import com.artagon.xacml.v3.AttributeCategory;

public interface ContentResolverDescriptor
{
	boolean canResolve(AttributeCategory category);
	Set<AttributeCategory> getSupportedCategories();
}
