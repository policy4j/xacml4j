package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.google.common.base.Preconditions;

/**
 * A base class for {@link ContentResolver} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseContentResolver implements ContentResolver
{
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
	public final Node getContent(AttributeCategory category, 
			PolicyInformationPointContext context)
	{
		Preconditions.checkArgument(descriptor.canResolve(category));
		return doResolve(category, context);
	}
	
	
	protected abstract Node doResolve(AttributeCategory category, 
			PolicyInformationPointContext context);

}
