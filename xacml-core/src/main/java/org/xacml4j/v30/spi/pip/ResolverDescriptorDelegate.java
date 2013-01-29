package org.xacml4j.v30.spi.pip;

import java.util.List;

import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeReferenceKey;

import com.google.common.base.Preconditions;

class ResolverDescriptorDelegate implements ResolverDescriptor
{
	private ResolverDescriptor d;

	protected ResolverDescriptorDelegate(ResolverDescriptor d){
		Preconditions.checkNotNull(d);
		this.d = d;
	}
	
	@Override
	public String getId() {
		return d.getId();
	}

	@Override
	public String getName() {
		return d.getName();
	}

	@Override
	public AttributeCategory getCategory() {
		return d.getCategory();
	}

	@Override
	public List<AttributeReferenceKey> getKeyRefs() {
		return d.getKeyRefs();
	}

	@Override
	public boolean isCachable() {
		return d.isCachable();
	}

	@Override
	public int getPreferreredCacheTTL() {
		return d.getPreferreredCacheTTL();
	}
}
