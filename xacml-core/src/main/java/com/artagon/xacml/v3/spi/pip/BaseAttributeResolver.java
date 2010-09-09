package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
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
	public final BagOfAttributeValues<AttributeValue> resolve(
			PolicyInformationPointContext context, AttributeDesignator ref,
			RequestContextAttributesCallback callback) throws Exception 
	{
		Preconditions.checkArgument(canResolve(ref));
		return doResolve(context, ref, callback);
	}
	
	protected abstract BagOfAttributeValues<AttributeValue> doResolve(PolicyInformationPointContext context, 
			AttributeDesignator ref, RequestContextAttributesCallback callback) 
				throws Exception;

	@Override
	public final boolean canResolve(AttributeDesignator ref)
	{
		if(descriptor.isCategorySupported(ref.getCategory()) && 
				((ref.getIssuer() != null)?ref.getIssuer().equals(descriptor.getIssuer()):true)){
			AttributeDescriptor d = descriptor.getAttributeDescriptor(
					ref.getCategory(), ref.getAttributeId());
			return d.getDataType().equals(ref.getDataType());
		}
		return false;
	}
}
