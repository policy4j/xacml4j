package com.artagon.xacml.v3.spi.pip;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeResolver implements AttributeResolver
{	
	private final static Logger log = LoggerFactory.getLogger(BaseAttributeResolver.class);
	
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
	public final AttributeSet resolve(
			PolicyInformationPointContext context) throws Exception 
	{
		if(log.isDebugEnabled()){
			log.debug("Retrieving attributes via resolver " +
					"id=\"{}\" name=\"{}\"", 
					descriptor.getId(), descriptor.getName());
		}
		return new AttributeSet(descriptor, doResolve(context));
	}
	
	protected abstract Map<String, BagOfAttributeValues> doResolve(
			PolicyInformationPointContext context);
	
}
