package com.artagon.xacml.v30.spi.pip;

import java.util.List;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeReferenceKey;
import com.google.common.base.Preconditions;

class ResolverDescriptorDelegate implements ResolverDescriptor
{
	private ResolverDescriptor d;

	protected ResolverDescriptorDelegate(ResolverDescriptor d){
		Preconditions.checkNotNull(d);
		this.d = d;
	}
	
	public String getId() {
		return d.getId();
	}

	public String getName() {
		return d.getName();
	}

	public AttributeCategory getCategory() {
		return d.getCategory();
	}

	public List<AttributeReferenceKey> getKeyRefs() {
		return d.getKeyRefs();
	}

	public boolean isCachable() {
		return d.isCachable();
	}

	public int getPreferreredCacheTTL() {
		return d.getPreferreredCacheTTL();
	}
	
	
}
