package com.artagon.xacml.v30.spi.pip;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.AttributeReferenceKey;
import com.artagon.xacml.v30.AttributeSelectorKey;
import com.artagon.xacml.v30.AttributeValueType;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public final class AttributeResolverDescriptorBuilder
{
	private String id;
	private String name;
	private AttributeCategory category;
	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private String issuer;
	private List<AttributeReferenceKey> keys;
	private int preferredCacheTTL = 0;
	
	private AttributeResolverDescriptorBuilder(
			String id, 
			String name, 
			String issuer, 
			AttributeCategory category){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.name = name;
		this.issuer = issuer;
		this.id = id;
		this.category = category;
		this.attributesById = new HashMap<String, AttributeDescriptor>();
		this.attributesByKey = new HashMap<AttributeDesignatorKey, AttributeDescriptor>();
		this.keys = new LinkedList<AttributeReferenceKey>();
	}
	
	public static AttributeResolverDescriptorBuilder builder(String id, 
			String name, AttributeCategory category){
		return builder(id, name, null, category);
	}
	
	public static AttributeResolverDescriptorBuilder builder(String id, 
			String name, String issuer, AttributeCategory category){
		return new AttributeResolverDescriptorBuilder(id, name, Strings.emptyToNull(issuer), category);
	}
	
	public AttributeResolverDescriptorBuilder designatorRef(AttributeCategory category, 
			String attributeId, AttributeValueType dataType, String issuer)
	{
		this.keys.add(new AttributeDesignatorKey(
				category, attributeId, dataType, Strings.emptyToNull(issuer)));
		return this;
	}
	
	public AttributeResolverDescriptorBuilder selectorRef(
			AttributeCategory category, 
			String xpath, AttributeValueType dataType, 
			String contextAttributeId)
	{
		this.keys.add(new AttributeSelectorKey(
				category, xpath, dataType, Strings.emptyToNull(contextAttributeId)));
		return this;
	}
	
	public AttributeResolverDescriptorBuilder keys(Iterable<AttributeReferenceKey> keys){
		Iterables.addAll(this.keys, keys);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder noCache(){
		this.preferredCacheTTL = -1;
		return this;
	}
	
	public AttributeResolverDescriptorBuilder cache(int ttl){
		this.preferredCacheTTL = ttl;
		return this;
	}
		
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, 
			AttributeValueType dataType){
		return attribute(attributeId, dataType, null);
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, 
			AttributeValueType dataType, 
			String[] defaultValue){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType);
		AttributeDesignatorKey key = new AttributeDesignatorKey(category, attributeId, dataType, issuer);
		Preconditions.checkState(attributesById.put(attributeId, d) == null,
				"Builder already has an attribute with id=\"%s\"", attributeId);
		Preconditions.checkState( this.attributesByKey.put(key, d) == null, 
				"Builder already has an attribute with id=\"%s\"", attributeId);
		if(issuer != null){
			AttributeDesignatorKey keyNoIssuer = new AttributeDesignatorKey(category, attributeId, dataType, null);
			Preconditions.checkState(attributesByKey.put(keyNoIssuer, d) == null, 
					"Builder already has an attribute with id=\"%s\"", attributeId);
		}
		return this;
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl();
	}

	private class AttributeResolverDescriptorImpl extends BaseResolverDescriptor
			implements AttributeResolverDescriptor 
	{
		private Map<String, AttributeDescriptor> attributesById;
		private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
		
		AttributeResolverDescriptorImpl() {
			super(id, name, category, keys, preferredCacheTTL);
			this.attributesById = ImmutableMap.copyOf(
					AttributeResolverDescriptorBuilder.this.attributesById);
			this.attributesByKey = ImmutableMap.copyOf(
					AttributeResolverDescriptorBuilder.this.attributesByKey);
		}
		
		@Override
		public String getIssuer() {
			return issuer;
		}
		
		@Override
		public int getAttributesCount(){
			return attributesById.size();
		}
		
		public Set<String> getProvidedAttributeIds(){
			return attributesById.keySet();
		}

		@Override
		public Map<String, AttributeDescriptor> getAttributesById() {
			return attributesById;
		}

		@Override
		public AttributeDescriptor getAttribute(String attributeId) {
			return attributesById.get(attributeId);
		}
		
		public Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey(){
			return attributesByKey;
		}

		@Override
		public boolean isAttributeProvided(String attributeId) {
			return attributesById.containsKey(attributeId);
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
