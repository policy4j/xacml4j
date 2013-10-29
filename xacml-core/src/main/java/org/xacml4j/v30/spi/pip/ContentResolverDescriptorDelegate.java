package org.xacml4j.v30.spi.pip;

import org.xacml4j.v30.AttributeCategory;

import com.google.common.base.Preconditions;

class ContentResolverDescriptorDelegate extends ResolverDescriptorDelegate
	implements ContentResolverDescriptor
{
	private ContentResolverDescriptor d;
	ContentResolverDescriptorDelegate(ContentResolverDescriptor d){
		super(d);
		Preconditions.checkNotNull(d);
		this.d = d;
	}

	@Override
	public boolean canResolve(AttributeCategory category) {
		return d.canResolve(category);
	}
}
