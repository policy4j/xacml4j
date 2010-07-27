package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.google.common.base.Preconditions;

public final class AttributeResolverDescriptorBuilder 
{
	private Map<AttributeCategoryId, Set<String>> attributes;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String issuer){
		this.issuer = issuer;
		this.attributes = new HashMap<AttributeCategoryId, Set<String>>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String issuer){
		return new AttributeResolverDescriptorBuilder(issuer);
	}
	
	public static AttributeResolverDescriptorBuilder create(){
		return create(null);
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			AttributeCategoryId category, String attributeId){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(attributeId);
		Set<String> v = attributes.get(category);
		if(v == null){
			v = new HashSet<String>();
			attributes.put(category, v);
		}
		v.add(attributeId);
		return this;
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverImpl();
	}
	
	final class AttributeResolverImpl implements AttributeResolverDescriptor
	{
		private String issuer;
		
		AttributeResolverImpl(){
			this.issuer = AttributeResolverDescriptorBuilder.this.issuer;
			Preconditions.checkState(attributes.keySet().size() > 0, 
					"At least one attribute category must be specified");
		}
		
		@Override
		public String getIssuer() {
			return issuer;
		}
		
		public boolean isAttributeProvided(AttributeCategoryId categoryId, 
				String attributeId){
			Set<String> v = attributes.get(categoryId);
			return (v == null)?false:v.contains(attributeId);
		}

		@Override
		public Set<String> getProvidedAttributes(AttributeCategoryId caregoryId) {
			Set<String> v = attributes.get(caregoryId);
			return (v == null)?Collections.<String>emptySet():Collections.unmodifiableSet(v);
		}

		@Override
		public Set<AttributeCategoryId> getProvidedCategories() {
			return Collections.unmodifiableSet(attributes.keySet());
		}
	}
}
