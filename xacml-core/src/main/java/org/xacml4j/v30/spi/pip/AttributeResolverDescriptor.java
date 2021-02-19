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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;

import java.util.*;
import java.util.function.Function;


public final class AttributeResolverDescriptor
	extends BaseResolverDescriptor<AttributeSet>
{
	private String issuer;
	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private Function<ResolverContext, Map<String, BagOfAttributeValues>> resolver;

	private AttributeResolverDescriptor(Builder b, Function<ResolverContext,
			Map<String, BagOfAttributeValues>> resolverFunction){
		super(b);
		this.issuer = b.issuer;
		this.attributesByKey = b.attributesByKey.build();
		this.attributesById = b.attributesById.build();
		this.resolver = Objects.requireNonNull(resolverFunction, "resolverFunction");
	}

	/**
	 * Gets an issuer identifier
	 * for this resolver attributes
	 *
	 * @return an issuer identifier
	 */
	public String getIssuer(){
		return issuer;
	}

	public boolean canResolve(AttributeReferenceKey category) {
		if(!(category instanceof AttributeSelectorKey)){
			return false;
		}
		return attributesByKey.containsKey(category);
	}
	@Override
	public Function<ResolverContext, Optional<AttributeSet>> getResolver() {
		return (context)->{
			Map<String, BagOfAttributeValues> v = resolver.apply(context);
			return Optional.ofNullable(v)
					.map(values->AttributeSet.builder(this)
							.clock(context.getClock())
							.attributes(values).build());
		};
	}

	/**
	 * Gets attribute of the given category
	 * with a given identifier descriptor
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link Optional<AttributeDescriptor>}
	 */
	public Optional<AttributeDescriptor> getAttribute(String attributeId){
		return Optional.ofNullable(attributesById.get(attributeId));
	}

	/**
	 * Gets number of attributes
	 * provided by this resolver
	 *
	 * @return a number of attributes
	 * provided by this resolver
	 */
	public int getAttributesCount(){
		return attributesById.size();
	}

	/**
	 * Gets a set of provided attribute identifiers
	 *
	 * @return an immutable {@link Set} of attribute identifiers
	 */
	public Set<String> getProvidedAttributeIds(){
		return attributesById.keySet();
	}

	/**
	 * Gets supported attributes
	 *
	 * @return a map by the attribute id
	 */
	public Map<String, AttributeDescriptor> getAttributesById(){
		return attributesById;
	}

	/**
	 * Gets map of attribute descriptors {@link AttributeDescriptor}
	 * mapped by the {@link AttributeDesignatorKey}
	 *
	 * @return map of {@link AttributeDescriptor} instances mapped
	 * by the {@link AttributeDesignatorKey}
	 */
	public Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey(){
		return attributesByKey;
	}

	/**
	 * Tests if an attribute resolver can resolve
	 * an attribute with a given identifier
	 *
	 * @param attributeId attribute identifier
	 * @return {@code true} if the attribute can be resolved
	 */
	public boolean canResolve(String attributeId){
		return attributesById.containsKey(attributeId);
	}

	static Builder builder(String id, String name, CategoryId category){
		return builder(id, name, null, category);
	}

	static Builder builder(String id, String name, String issuer, CategoryId category){
		return new Builder(id, name, issuer, category);
	}

	final static class Builder
			extends BaseBuilder<AttributeSet, Builder>
	{
		private static Logger LOG = LoggerFactory.getLogger(Builder.class);

		private ImmutableMap.Builder<String, AttributeDescriptor> attributesById;
		private ImmutableMap.Builder<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
		private String issuer;

		private Builder(
				String id,
				String name,
				String issuer,
				CategoryId categoryId){
			super(id, name, categoryId);
			this.issuer = Strings.emptyToNull(issuer);
			this.attributesById = ImmutableMap.builder();
			this.attributesByKey = ImmutableMap.builder();
		}

		public Builder  attribute(
				String attributeId,
				AttributeValueType dataType,
				Collection<String> aliases){
			AttributeDescriptor d = AttributeDescriptor.of(attributeId, dataType, aliases);
			LOG.debug("Adding attributeId=\"{}\" aliases=\"{}\"", attributeId, aliases);
			for(String alias : d.getAliases() ){
				AttributeDesignatorKey k = AttributeDesignatorKey.builder()
						.category(categoryId)
						.attributeId(alias)
						.dataType(dataType)
						.issuer(issuer)
						.build();
				if(attributesById.put(alias, d) != null){
					throw new IllegalArgumentException(String.format(
							"Builder already has an attribute with id=\"%s\"", alias));
				}
				if(this.attributesByKey.put(k, d) != null){
					throw new IllegalArgumentException(String.format(
							"Builder already has an attribute with id=\"%s\"", alias));
				}
			}
			return this;
		}

		public Builder  attribute(
				String attributeId,
				AttributeValueType dataType, String ... aliases){
			return attribute(attributeId, dataType,
					(aliases == null)?Collections.emptyList(): ImmutableList.copyOf(aliases));
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeResolverDescriptor build(Function<ResolverContext, Map<String, BagOfAttributeValues>> resolverFunction){
			return new AttributeResolverDescriptor(this, resolverFunction);
		}
	}
}
