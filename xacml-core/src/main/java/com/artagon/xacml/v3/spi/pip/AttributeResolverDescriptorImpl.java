package com.artagon.xacml.v3.spi.pip;

import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

final class AttributeResolverDescriptorImpl implements
		AttributeResolverDescriptor {
	private String issuer;
	private Map<String, AttributeDescriptor> attributes;
	private Set<AttributeCategoryId> categories;

	AttributeResolverDescriptorImpl(String issuer,
			Set<AttributeCategoryId> categories,
			Map<String, AttributeDescriptor> attributes) {
		this.attributes = ImmutableMap.copyOf(attributes);
		this.categories = ImmutableSet.copyOf(categories);
		this.issuer = issuer;
	}

	@Override
	public String getIssuer() {
		return issuer;
	}

	@Override
	public boolean isCategorySupported(AttributeCategoryId category) {
		return categories.contains(category);
	}

	@Override
	public Set<AttributeCategoryId> getSupportedCategores() {
		return categories;
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeId) {
		return attributes.get(attributeId);
	}

	public boolean isAttributeProvided(String attributeId) {
		return attributes.containsKey(attributeId);
	}

	@Override
	public Set<String> getProvidedAttributeIds() {
		return attributes.keySet();
	}
}
