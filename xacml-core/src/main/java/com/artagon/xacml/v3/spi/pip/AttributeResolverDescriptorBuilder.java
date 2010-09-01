package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;

public final class AttributeResolverDescriptorBuilder 
{
	private AttributeCategoryId categoryId;
	private Map<String, AttributeDescriptor> attributes;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String issuer, 
			AttributeCategoryId categoryId){
		this.issuer = issuer;
		this.categoryId = categoryId;
		this.attributes = new HashMap<String, AttributeDescriptor>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String issuer, 
			AttributeCategoryId categoryId){
		return new AttributeResolverDescriptorBuilder(issuer, categoryId);
	}
	
	public static AttributeResolverDescriptorBuilder create(AttributeCategoryId categoryId){
		return create(null, categoryId);
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, AttributeValueType dataType){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType);
		attributes.put(d.getAttributeId(), d);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, XacmlDataTypes dataType){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType.getType());
		attributes.put(d.getAttributeId(), d);
		return this;
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl();
	}
	
	final class AttributeResolverDescriptorImpl 
		implements AttributeResolverDescriptor
	{
		private String issuer;
		
		AttributeResolverDescriptorImpl(){
			this.issuer = AttributeResolverDescriptorBuilder.this.issuer;
			Preconditions.checkState(attributes.size() > 0, 
					"At least one attribute  must be specified");
		}
		
		@Override
		public String getIssuer() {
			return issuer;
		}
		
		public boolean isAttributeProvided( String attributeId){
			return attributes.containsKey(attributeId);
		}

		@Override
		public Set<String> getProvidedAttributeIds() {
			return Collections.unmodifiableSet(attributes.keySet());
		}

		@Override
		public AttributeCategoryId getCategory() {
			return categoryId;
		}
	}
}
