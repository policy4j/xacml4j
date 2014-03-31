package org.xacml4j.v30.spi.pip;

import org.xacml4j.v30.CategoryId;

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
	public boolean canResolve(CategoryId category) {
		return d.canResolve(category);
	}
}
