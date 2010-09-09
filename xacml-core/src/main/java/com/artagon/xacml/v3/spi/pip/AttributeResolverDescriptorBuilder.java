package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class AttributeResolverDescriptorBuilder 
{
	private Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String issuer){
		this.issuer = issuer;
		this.attributes = new HashMap<AttributeCategoryId, Map<String,AttributeDescriptor>>();
	}
	
	public static AttributeResolverDescriptorBuilder create(){
		return create(null);
	}
	
	public static AttributeResolverDescriptorBuilder create(String issuer){
		return new AttributeResolverDescriptorBuilder(issuer);
	}
		
	public AttributeResolverDescriptorBuilder attribute(
			AttributeCategoryId category,
			String attributeId, 
			AttributeValueType dataType){
		Map<String, AttributeDescriptor> byId = attributes.get(category);
		if(byId == null){
			byId = new HashMap<String, AttributeDescriptor>();
			attributes.put(category, byId);
		}
		AttributeDescriptor old = byId.put(attributeId, new AttributeDescriptor(attributeId, dataType));
		Preconditions.checkState(old == null, 
				"Given category=\"%s\" already has an attribute with id=\"%s\"", 
				category, attributeId);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			AttributeCategoryId category,
			String attributeId, XacmlDataTypes dataType){
		return attribute(category, attributeId, dataType.getType());
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl(issuer, attributes);
	}

	private class AttributeResolverDescriptorImpl implements
			AttributeResolverDescriptor 
	{
		private String issuer;
		private Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes;
		
		AttributeResolverDescriptorImpl(String issuer,
				Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes) {
			this.attributes = new HashMap<AttributeCategoryId, Map<String,AttributeDescriptor>>();
			for(AttributeCategoryId c : attributes.keySet()){
				this.attributes.put(c, ImmutableMap.copyOf(attributes.get(c)));
			}
			this.issuer = issuer;
		}

		@Override
		public String getIssuer() {
			return issuer;
		}

		@Override
		public boolean isCategorySupported(AttributeCategoryId category) {
			return attributes.containsKey(category);
		}

		@Override
		public Set<AttributeCategoryId> getSupportedCategores() {
			return attributes.keySet();
		}
		
		public Set<String> getProvidedAttributeIds(AttributeCategoryId category){
			Map<String, AttributeDescriptor> byId = attributes.get(category);
			return (byId == null)?Collections.<String>emptySet():byId.keySet();
		}

		@Override
		public Map<String, AttributeDescriptor> getAttributes(
				AttributeCategoryId category) {
			Map<String, AttributeDescriptor> byId = attributes.get(category);
			return (byId == null)?Collections.<String, AttributeDescriptor>emptyMap():byId;
		}


		@Override
		public AttributeDescriptor getAttributeDescriptor(AttributeCategoryId category, 
				String attributeId) {
			Map<String, AttributeDescriptor> byId = attributes.get(category);
			return (byId == null)?null:byId.get(attributeId);
		}

		public boolean isAttributeProvided(AttributeCategoryId category, String attributeId) {
			Map<String, AttributeDescriptor> byId = attributes.get(category);
			return (byId == null)?false:byId.containsKey(attributeId);
		}
	}
}
