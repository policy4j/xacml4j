package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public final class AttributeResolverDescriptorBuilder 
{
	private Set<AttributeCategoryId> categories;
	private Set<String> attributeIds;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String issuer){
		this.issuer = issuer;
		this.categories = new HashSet<AttributeCategoryId>();
		this.attributeIds = new HashSet<String>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String issuer){
		return new AttributeResolverDescriptorBuilder(issuer);
	}
	
	public static AttributeResolverDescriptorBuilder create(){
		return create(null);
	}
	
	public AttributeResolverDescriptorBuilder withCategory(AttributeCategoryId category){
		Preconditions.checkNotNull(category);
		this.categories.add(category);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder withAttribute(String attributeId){
		Preconditions.checkNotNull(attributeId);
		this.attributeIds.add(attributeId);
		return this;
	}
	
	public AttributeResolverDescriptor build(){
		return new AttributeResolverImpl();
	}
	
	final class AttributeResolverImpl implements AttributeResolverDescriptor
	{
		private String issuer;
		private Set<String> providedAttributeIds;
		private Set<AttributeCategoryId> providedAttributeCategories;
		
		AttributeResolverImpl(){
			this.issuer = AttributeResolverDescriptorBuilder.this.issuer;
			this.providedAttributeCategories = Sets.immutableEnumSet(AttributeResolverDescriptorBuilder.this.categories);
			this.providedAttributeIds = Collections.unmodifiableSet(AttributeResolverDescriptorBuilder.this.attributeIds);
			
			Preconditions.checkState(providedAttributeCategories.size() > 0, 
					"At least one attribute category must be specified");
			Preconditions.checkState(providedAttributeIds.size() > 0, 
					"At least one attribute identifier must be specified");
		}
		
		@Override
		public String getIssuer() {
			return issuer;
		}

		@Override
		public Set<String> getProvidedAttributes() {
			return providedAttributeIds;
		}

		@Override
		public Set<AttributeCategoryId> getProvidedCategories() {
			return providedAttributeCategories;
		}
	}
}
