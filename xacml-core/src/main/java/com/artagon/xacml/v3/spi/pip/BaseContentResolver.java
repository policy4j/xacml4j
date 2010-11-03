package com.artagon.xacml.v3.spi.pip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;

/**
 * A base class for {@link ContentResolver} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseContentResolver implements ContentResolver
{
	private final static Logger log = LoggerFactory.getLogger(BaseContentResolver.class);
	
	private ContentResolverDescriptor descriptor;

	protected BaseContentResolver(ContentResolverDescriptor descriptor){
		Preconditions.checkArgument(descriptor != null);
		this.descriptor = descriptor;
	}
	
	@Override
	public final ContentResolverDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public final Node resolve(
			PolicyInformationPointContext context, BagOfAttributeValues ...keys) throws Exception 
	{
		if(log.isDebugEnabled()){
			log.debug("Retrieving content via resolver " +
					"id=\"{}\" name=\"{}\"", descriptor.getId(), descriptor.getName());
		}
		return doResolve(context, keys);	
	}
	
	protected abstract Node doResolve(
			PolicyInformationPointContext context, BagOfAttributeValues ...keys) 
		throws Exception;
}
