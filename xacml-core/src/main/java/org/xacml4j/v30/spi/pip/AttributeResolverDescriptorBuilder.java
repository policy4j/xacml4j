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

import java.util.*;
import java.util.function.Function;

import org.xacml4j.v30.*;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

public final class AttributeResolverDescriptorBuilder
		extends ResolverDescriptorBuilder<AttributeResolverDescriptorBuilder>
{

	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private String issuer;
	private Function<ResolverContext, Optional<AttributeSet>> resolveFunction;

	private AttributeResolverDescriptorBuilder(
			String id,
			String name,
			String issuer,
			CategoryId categoryId,
			CategoryId ... categories){
		super(id, name, categoryId, categories));
		this.issuer = Strings.emptyToNull(issuer);
		this.attributesById = new LinkedHashMap<>();
		this.attributesByKey = new LinkedHashMap<>();
	}

	public static AttributeResolverDescriptorBuilder builder(String id,
															 String name,
															 CategoryId categoryId,
															 CategoryId  ... category){
		return builder(id, name, categoryId, category);
	}

	public static AttributeResolverDescriptorBuilder builder(String id,
			String name, String issuer, CategoryId categoryId, CategoryId ... category){
		return new AttributeResolverDescriptorBuilder(id, name, issuer, categoryId, category);
	}

	public AttributeResolverDescriptorBuilder resolver(Function<ResolverContext, Optional<AttributeSet>> resolveFunction){
		this.resolveFunction = Objects.requireNonNull(resolveFunction, "resolveFunction");
	}

	public AttributeResolverDescriptorBuilder attribute(
			String attributeId,
			AttributeValueType dataType, String ...aliases){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType, aliases);
		Collection<String> attributeIds = Arrays.asList(aliases);
		attributeIds.add(attributeId);
		for(CategoryId categoryId : allCategories){
			for(String id : attributeIds){
				AttributeDesignatorKey k = AttributeDesignatorKey.builder()
						.category(categoryId)
						.attributeId(attributeId)
						.dataType(dataType)
						.issuer(issuer)
						.build();
				Preconditions.checkState(attributesById.put(id, d) == null,
						"Builder already has an attribute with id=\"%s\"", id);
				Preconditions.checkState( this.attributesByKey.put(k, d) == null,
						"Builder already has an attribute with id=\"%s\"", id);
			}
		}
		return this;
	}

	@Override
	protected AttributeResolverDescriptorBuilder getThis() {
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
			super(id, name, allCategories, cacheTTL, contextKeysResolutionPlan);
			this.attributesById = ImmutableMap.copyOf(
					AttributeResolverDescriptorBuilder.this.attributesById);
			this.attributesByKey = ImmutableMap.copyOf(
					AttributeResolverDescriptorBuilder.this.attributesByKey);
			this.
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
		public boolean canResolve(String attributeId) {
			return attributesById.containsKey(attributeId);
		}


		@Override
		public boolean canResolve(AttributeReferenceKey referenceKey)
		{
			if(!(referenceKey instanceof AttributeDesignatorKey)){
				return false;
			}
			AttributeDesignatorKey ref  = (AttributeDesignatorKey)referenceKey;
			if(getCategoryIds().contains(referenceKey.getCategory()) &&
					((ref.getIssuer() == null) || ref.getIssuer().equals(getIssuer()))){
				AttributeDescriptor d = getAttribute(ref.getAttributeId());
				return (d != null) && d.getDataType().equals(ref.getDataType());
			}
			return false;
		}

		@Override
		public Collection<CategoryId> getSupportedCategories() {
			return allCategories;
		}
	}
}
