package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.google.common.base.Preconditions;

public final class AttributeResolverDescriptorBuilder 
{
	private AttributeCategoryId categoryId;
	private Set<String> attributes;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String issuer, 
			AttributeCategoryId categoryId){
		this.issuer = issuer;
		this.categoryId = categoryId;
		this.attributes = new HashSet<String>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String issuer, 
			AttributeCategoryId categoryId){
		return new AttributeResolverDescriptorBuilder(issuer, categoryId);
	}
	
	public static AttributeResolverDescriptorBuilder create(AttributeCategoryId categoryId){
		return create(null, categoryId);
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId){
	
		Preconditions.checkNotNull(attributeId);
		this.attributes.add(attributeId);
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
			Preconditions.checkState(attributes.size() > 0, 
					"At least one attribute  must be specified");
		}
		
		@Override
		public String getIssuer() {
			return issuer;
		}
		
		public boolean isAttributeProvided( String attributeId){
			return attributes.contains(attributeId);
		}

		@Override
		public Set<String> getProvidedAttributes() {
			return Collections.unmodifiableSet(attributes);
		}

		@Override
		public AttributeCategoryId getCategory() {
			return categoryId;
		}
	}
}
