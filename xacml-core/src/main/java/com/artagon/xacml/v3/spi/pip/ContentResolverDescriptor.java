package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;

import java.util.Set;

public interface ContentResolverDescriptor
{
	boolean canResolve(AttributeCategory category);
	Set<AttributeCategory> getSupportedCategories();
}
