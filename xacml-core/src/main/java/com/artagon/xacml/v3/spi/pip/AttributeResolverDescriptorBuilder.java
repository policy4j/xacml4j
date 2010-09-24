package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class AttributeResolverDescriptorBuilder 
{
	private String name;
	private Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes;
	private String issuer;
	
	private AttributeResolverDescriptorBuilder(String name, String issuer){
		this.name = name;
		this.issuer = issuer;
		this.attributes = new HashMap<AttributeCategoryId, Map<String,AttributeDescriptor>>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String name){
		return create(name, null);
	}
	
	public static AttributeResolverDescriptorBuilder create(String name, String issuer){
		return new AttributeResolverDescriptorBuilder(name, issuer);
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
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl(name, issuer, attributes);
	}

	private class AttributeResolverDescriptorImpl implements
			AttributeResolverDescriptor 
	{
		private String name;
		private String issuer;
		private Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes;
		
		AttributeResolverDescriptorImpl(
				String name,
				String issuer,
				Map<AttributeCategoryId, Map<String, AttributeDescriptor>> attributes) {
			Preconditions.checkArgument(name != null);
			this.attributes = new HashMap<AttributeCategoryId, Map<String,AttributeDescriptor>>();
			for(AttributeCategoryId c : attributes.keySet()){
				this.attributes.put(c, ImmutableMap.copyOf(attributes.get(c)));
			}
			this.name = name;
			this.issuer = issuer;
		}
		
		@Override
		public String getName(){
			return name;
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

		@Override
		public boolean isAttributeProvided(AttributeCategoryId category, String attributeId) {
			Map<String, AttributeDescriptor> byId = attributes.get(category);
			return (byId == null)?false:byId.containsKey(attributeId);
		}
		
		@Override
		public boolean canResolve(AttributeCategoryId category, 
				String attributeId, AttributeValueType dataType, String issuer)
		{
			if(isCategorySupported(category) && 
					((issuer != null)?issuer.equals(getIssuer()):true)){
				AttributeDescriptor d = getAttributeDescriptor(
						category, attributeId);
				return d.getDataType().equals(dataType);
			}
			return false;
		}
	}
}
