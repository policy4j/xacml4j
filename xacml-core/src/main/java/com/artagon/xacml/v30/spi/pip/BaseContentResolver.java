package com.artagon.xacml.v30.spi.pip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

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
	public final Content resolve(
			ResolverContext context) throws Exception 
	{
		Preconditions.checkArgument(context.getDescriptor() == descriptor);
		if(log.isDebugEnabled()){
			log.debug("Retrieving content via resolver " +
					"id=\"{}\" name=\"{}\"", descriptor.getId(), descriptor.getName());
		}
		return new Content(descriptor, doResolve(context));	
	}
	
	/**
	 * Performs an actual content resolution
	 * 
	 * @param context a policy information context
	 * @return {@link Node} a resolved content or <code>null</code>
	 * @throws Exception if an error occurs
	 */
	protected abstract Node doResolve(
			ResolverContext context) 
		throws Exception;
}
