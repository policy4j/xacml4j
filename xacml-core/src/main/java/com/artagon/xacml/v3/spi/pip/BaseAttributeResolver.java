package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeDesignator;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeResolver implements AttributeResolver
{
	private AttributeResolverDescriptor descriptor;
	
	protected BaseAttributeResolver(AttributeResolverDescriptor descriptor){
		Preconditions.checkNotNull(descriptor);
		this.descriptor = descriptor;
	}
	
	@Override
	public final AttributeResolverDescriptor getDescriptor(){
		return descriptor;
	}
	
	public final boolean canResolve(AttributeDesignator ref)
	{
		return (ref.getIssuer() != null && 
				ref.getIssuer().equals(descriptor.getIssuer())) 
				&& descriptor.isAttributeProvided(ref.getAttributeId());
	}
}
