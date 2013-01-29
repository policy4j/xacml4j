package com.artagon.xacml.v30.spi.pip;

import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.google.common.base.Preconditions;

class AttributeResolverDescriptorDelegate 
	extends ResolverDescriptorDelegate implements AttributeResolverDescriptor
{
	private AttributeResolverDescriptor d;

	AttributeResolverDescriptorDelegate(AttributeResolverDescriptor d){
		super(d);
		Preconditions.checkNotNull(d);
		this.d = d;
	}
	
	@Override
	public boolean canResolve(AttributeDesignatorKey key) {
		return d.canResolve(key);
	}

	@Override
	public AttributeDescriptor getAttribute(String attributeId) {
		return d.getAttribute(attributeId);
	}

	@Override
	public int getAttributesCount() {
		return d.getAttributesCount();
	}

	@Override
	public Set<String> getProvidedAttributeIds() {
		return d.getProvidedAttributeIds();
	}

	@Override
	public Map<String, AttributeDescriptor> getAttributesById() {
		return d.getAttributesById();
	}

	@Override
	public Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey() {
		return d.getAttributesByKey();
	}

	@Override
	public boolean isAttributeProvided(String attributeId) {
		return d.isAttributeProvided(attributeId);
	}

	@Override
	public String getIssuer() {
		return d.getIssuer();
	}
}
