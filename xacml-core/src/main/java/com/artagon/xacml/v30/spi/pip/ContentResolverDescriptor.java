package com.artagon.xacml.v30.spi.pip;

import com.artagon.xacml.v30.AttributeCategory;

public interface ContentResolverDescriptor extends ResolverDescriptor
{
	boolean canResolve(AttributeCategory category);
}
