package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;

public interface ContentResolverDescriptor extends ResolverDescriptor
{
	boolean canResolve(AttributeCategory category);
}
