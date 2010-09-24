package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
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
	public final BagOfAttributeValues resolve(
			PolicyInformationPointContext context, 
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) throws Exception 
	{
		Preconditions.checkArgument(descriptor.canResolve(category, attributeId, dataType, issuer));
		return doResolve(context, category, attributeId, dataType, issuer);
	}
	
	protected abstract BagOfAttributeValues doResolve(
			PolicyInformationPointContext context, 
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) throws Exception;
}
