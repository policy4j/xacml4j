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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public interface AttributeResolverDescriptor
	extends ResolverDescriptor
{
	/**
	 * Gets an issuer identifier
	 * for this resolver attributes
	 *
	 * @return an issuer identifier
	 */
	String getIssuer();

	/**
	 * Gets attribute of the given category
	 * with a given identifier descriptor
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link AttributeDescriptor}
	 */
	AttributeDescriptor getAttribute(String attributeId);

	/**
	 * Gets number of attributes
	 * provided by this resolver
	 *
	 * @return a number of attributes
	 * provided by this resolver
	 */
	int getAttributesCount();

	/**
	 * Gets a set of provided attribute identifiers
	 *
	 * @return an immutable {@link Set} of attribute identifiers
	 */
	Set<String> getProvidedAttributeIds();

	/**
	 * Gets supported attributes
	 *
	 * @return a map by the attribute id
	 */
	Map<String, AttributeDescriptor> getAttributesById();

	/**
	 * Gets map of attribute descriptors {@link AttributeDescriptor}
	 * mapped by the {@link AttributeDesignatorKey}
	 *
	 * @return map of {@link AttributeDescriptor} instances mapped
	 * by the {@link AttributeDesignatorKey}
	 */
	Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey();


	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 *
	 * @param attributeId attribute identifier
	 * @return {@code true} if the attribute can be resolved
	 */
	boolean canResolve(String attributeId);

	static Builder builder(String id, String name, CategoryId  ... category){
		return builder(id, name, null, category);
	}

	static Builder builder(String id, String name, String issuer, CategoryId ... category){
		return new Builder(id, name, issuer, category);
	}



	final class Builder
			extends ResolverDescriptorBuilder<Builder>
	{

		private Map<String, AttributeDescriptor> attributesById;
		private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
		private String issuer;

		private Builder(
				String id,
				String name,
				String issuer,
				CategoryId... category){
			super(id, name, category);
			this.issuer = Strings.emptyToNull(issuer);
			this.attributesById = new LinkedHashMap<>();
			this.attributesByKey = new LinkedHashMap<>();
		}



		public Builder  attribute(
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
		protected Builder getThis() {
			return this;
		}

		private AttributeResolverDescriptor build(){
			return new AttributeResolverDescriptorImpl();
		}

		public Resolver<AttributeSet> build(Function<ResolverContext, Map<String, BagOfAttributeValues>> resolverFunction){
			return new AttributeResolver(build(), resolverFunction);
		}

		private class AttributeResolverDescriptorImpl extends BaseResolverDescriptor
				implements AttributeResolverDescriptor
		{
			private Map<String, AttributeDescriptor> attributesById;
			private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;

			AttributeResolverDescriptorImpl() {
				super(id, name, allCategories, cacheTTL, contextKeysResolutionPlan);
				this.attributesById = ImmutableMap.copyOf(
						Builder.this.attributesById);
				this.attributesByKey = ImmutableMap.copyOf(
						Builder.this.attributesByKey);
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

	final class AttributeResolver implements Resolver<AttributeSet>
	{
		protected final Logger log = LoggerFactory.getLogger(getClass());

		private final AttributeResolverDescriptor descriptor;

		private Function<ResolverContext, Map<String, BagOfAttributeValues>> function;

		private AttributeResolver(
				AttributeResolverDescriptor descriptor,
				java.util.function.Function<ResolverContext, Map<String, BagOfAttributeValues>> function){
			checkNotNull(descriptor);
			this.descriptor = Objects.requireNonNull(descriptor, AttributeResolverDescriptor.class.getSimpleName());
			this.function = Objects.requireNonNull(function, AttributeResolverDescriptor.class.getSimpleName());

		}

		@Override
		public final AttributeResolverDescriptor getDescriptor(){
			return descriptor;
		}

		@Override
		public final Optional<AttributeSet> resolve(
				ResolverContext context)
		{
			checkArgument(context.getDescriptor().getId().equals(descriptor.getId()));
			if(log.isDebugEnabled()){
				log.debug("Retrieving attributes via resolver id=\"{}\" name=\"{}\"",
						descriptor.getId(), descriptor.getName());
			}
			Map<String, BagOfAttributeValues> attributeValuesMap = function.apply(context);
			if(attributeValuesMap == null ||
					attributeValuesMap.isEmpty()){
				return Optional.empty();
			}
			return Optional.of(attributeValuesMap)
					.map(v->AttributeSet.builder(this)
							.attributes(v)
							.ticker(context.getClock())
							.build());
		}
	}

}
