package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.CategoryId;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public final class AttributeResolverDescriptorBuilder
{
	private String id;
	private String name;
	private CategoryId category;
	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private String issuer;
	private List<AttributeReferenceKey> keys;
	private int preferredCacheTTL = 0;

	private AttributeResolverDescriptorBuilder(
			String id,
			String name,
			String issuer,
			CategoryId category){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.name = name;
		this.issuer = Strings.emptyToNull(issuer);
		// JMX friendly name
		this.id = id.replace(":", ".");
		this.category = category;
		this.attributesById = new LinkedHashMap<String, AttributeDescriptor>();
		this.attributesByKey = new LinkedHashMap<AttributeDesignatorKey, AttributeDescriptor>();
		this.keys = new LinkedList<AttributeReferenceKey>();
	}

	public static AttributeResolverDescriptorBuilder builder(String id,
			String name, CategoryId category){
		return builder(id, name, null, category);
	}

	public static AttributeResolverDescriptorBuilder builder(String id,
			String name, String issuer, CategoryId category){
		return new AttributeResolverDescriptorBuilder(id, name, issuer, category);
	}

	public AttributeResolverDescriptorBuilder designatorKeyRef(CategoryId category,
			String attributeId, AttributeExpType dataType, String issuer)
	{
		this.keys.add(AttributeDesignatorKey
				.builder()
				.category(category)
				.attributeId(attributeId)
				.dataType(dataType)
				.issuer(issuer).build());
		return this;
	}

	public AttributeResolverDescriptorBuilder requestContextKey(CategoryId category,
			String attributeId, AttributeExpType dataType)
	{
		return designatorKeyRef(category, attributeId, dataType, null);
	}

	public AttributeResolverDescriptorBuilder requestContextKey(
			CategoryId category,
			String xpath, AttributeExpType dataType,
			String contextAttributeId)
	{
		this.keys.add(AttributeSelectorKey
				.builder()
				.category(category)
				.xpath(xpath)
				.dataType(dataType)
				.contextSelectorId(contextAttributeId)
				.build());
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
			AttributeExpType dataType){
		return attribute(attributeId, dataType, null);
	}

	public AttributeResolverDescriptorBuilder attributes(
			Iterable<String> attributeIds,
			AttributeExpType dataType){
		for(String attributeId : attributeIds){
			attribute(attributeId, dataType);
		}
		return this;
	}

	public AttributeResolverDescriptorBuilder attribute(
			String attributeId,
			AttributeExpType dataType,
			String[] defaultValue){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType);
		AttributeDesignatorKey key = AttributeDesignatorKey.builder()
				.category(category)
				.attributeId(attributeId)
				.dataType(dataType)
				.issuer(issuer)
				.build();
		Preconditions.checkState(attributesById.put(attributeId, d) == null,
				"Builder already has an attribute with id=\"%s\"", attributeId);
		Preconditions.checkState( this.attributesByKey.put(key, d) == null,
				"Builder already has an attribute with id=\"%s\"", attributeId);
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
			for(AttributeReferenceKey k : keys){
				if(k instanceof AttributeDesignatorKey){
					Preconditions.checkArgument(!canResolve((AttributeDesignatorKey)k),
							"Resolver refers to itself via context reference key=\"{}\"", k);
				}
			}
		}

		@Override
		public String getIssuer() {
			return issuer;
		}

		@Override
		public int getAttributesCount(){
			return attributesById.size();
		}

		@Override
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

		@Override
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
					((ref.getIssuer() == null) || ref.getIssuer().equals(getIssuer()))){
				AttributeDescriptor d = getAttribute(ref.getAttributeId());
				return (d != null) && d.getDataType().equals(ref.getDataType());
			}
			return false;
		}
	}
}
