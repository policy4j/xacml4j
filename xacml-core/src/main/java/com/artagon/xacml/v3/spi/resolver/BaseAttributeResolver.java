package com.artagon.xacml.v3.spi.resolver;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.RequestAttributesCallback;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.AttributeResolver;
import com.artagon.xacml.v3.spi.AttributeResolverDescriptor;
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

	@Override
	public final BagOfAttributeValues<? extends AttributeValue> resolve(
			AttributeDesignator ref, RequestAttributesCallback callback) 
	{
		Preconditions.checkArgument(ref != null);
		Preconditions.checkArgument(callback != null);
		Preconditions.checkArgument(descriptor.getProvidedCategories().contains(ref.getCategory()));
		Preconditions.checkArgument(descriptor.getProvidedAttributes().contains(ref.getAttributeId()));
		return doResolve(ref, callback);
	}
	
	protected abstract BagOfAttributeValues<? extends AttributeValue> doResolve(
			AttributeDesignator ref, RequestAttributesCallback callback);
}
