package com.artagon.xacml.v3.spi.pip;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.AttributeValueType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public final class AttributeResolverDescriptorBuilder
{
	private String id;
	private String name;
	private AttributeCategory category;
	private Map<String, AttributeDescriptor> attributes;
	private String issuer;
	private List<AttributeReferenceKey> keys;
	private long preferredCacheTTL = 0;
	
	private AttributeResolverDescriptorBuilder(
			String id, String name, 
			String issuer, AttributeCategory category){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.name = name;
		this.issuer = issuer;
		this.id = id;
		this.category = category;
		this.attributes = new HashMap<String, AttributeDescriptor>();
		this.keys = new LinkedList<AttributeReferenceKey>();
	}
	
	public static AttributeResolverDescriptorBuilder create(String id, 
			String name, AttributeCategory category){
		return create(id, name, null, category);
	}
	
	public static AttributeResolverDescriptorBuilder create(String id, 
			String name, String issuer, AttributeCategory category){
		return new AttributeResolverDescriptorBuilder(id, name, issuer, category);
	}
	
	public AttributeResolverDescriptorBuilder noCache(){
		this.preferredCacheTTL = -1;
		return this;
	}
	
	public AttributeResolverDescriptorBuilder cache(long ttl){
		this.preferredCacheTTL = ttl;
		return this;
	}
		
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, 
			AttributeValueType dataType){
		AttributeDescriptor old = attributes.put(attributeId, new AttributeDescriptor(attributeId, dataType));
		Preconditions.checkState(old == null, 
				"Builder already has an attribute with id=\"%s\"", attributeId);
		return this;
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl();
	}

	private class AttributeResolverDescriptorImpl extends BaseResolverDescriptor
			implements AttributeResolverDescriptor 
	{
		private Map<String, AttributeDescriptor> attributes;
		
		AttributeResolverDescriptorImpl() {
			super(id, name, category, keys, preferredCacheTTL);
			this.attributes = ImmutableMap.copyOf(AttributeResolverDescriptorBuilder.this.attributes);
		}
		
		
		@Override
		public String getIssuer() {
			return issuer;
		}
		
		
		public Set<String> getProvidedAttributeIds(){
			return attributes.keySet();
		}

		@Override
		public Map<String, AttributeDescriptor> getAttributes() {
			return attributes;
		}

		@Override
		public AttributeDescriptor getAttribute(String attributeId) {
			return attributes.get(attributeId);
		}

		@Override
		public boolean isAttributeProvided(String attributeId) {
			return attributes.containsKey(attributeId);
		}
		
		@Override
		public boolean canResolve(AttributeDesignatorKey ref)
		{
			if(getCategory().equals(ref.getCategory()) && 
					((ref.getIssuer() != null)?ref.getIssuer().equals(getIssuer()):true)){
				AttributeDescriptor d = getAttribute(ref.getAttributeId());
				return (d != null)?d.getDataType().equals(ref.getDataType()):false;
			}
			return false;
		}
	}
}
