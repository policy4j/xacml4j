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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;


public final class AttributeResolverDescriptor
	extends BaseResolverDescriptor<AttributeSet>
{
	private final static Logger LOG = LoggerFactory.getLogger(AttributeResolverDescriptor.class);
	private String issuer;
	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private Function<ResolverContext, Optional<AttributeSet>> resolver;

	private AttributeResolverDescriptor(Builder b, Function<ResolverContext,
			Map<String, BagOfValues>> resolverFunction){
		super(b);
		this.issuer = b.issuer;
		this.attributesByKey = ImmutableMap.copyOf(b.attributesByKey);
		this.attributesById = ImmutableMap.copyOf(b.attributesById);
		this.resolver = decorate(Objects.requireNonNull(resolverFunction, "resolverFunction"));
		for(AttributeReferenceKey k : attributesByKey.keySet()){
			if(getContextKeyRefs().contains(k)){
				throw SyntaxException.invalidResolverReferenceSelfRef(getId(), "key={}", k);
			}
		}
	}

	private Function<ResolverContext, Optional<AttributeSet>> decorate(Function<ResolverContext,
			Map<String, BagOfValues>> resolverFunction)
	{
		return (context)->{
			try{
				Map<String, BagOfValues> v = resolverFunction.apply(context);
				LOG.error("Resolver values={}", v);
				return Optional.ofNullable(v)
				               .flatMap(values->values.isEmpty()?Optional.empty():
				                                Optional.of(AttributeSet
						                                            .builder(this)
						                                            .clock(context.getClock())
						                                            .attributes(values).build()));
			}catch (EvaluationException e){
				throw e;
			}
			catch (Exception e){
				throw new EvaluationException(context.getEvaluationContext(), e);
			}
		};
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

	public boolean canResolve(AttributeReferenceKey referenceKey) {
		if((referenceKey instanceof AttributeSelectorKey)){
			return false;
		}
		if((referenceKey instanceof AttributeDesignatorKey)){
			AttributeDesignatorKey designatorKey = (AttributeDesignatorKey) referenceKey;
			LOG.debug("AttributesByKey={}", attributesByKey.keySet());
			return attributesByKey.keySet()
			               .stream()
			                      .filter(v->match(designatorKey, v))
			                      .findFirst().isPresent();
		}
		return false;
	}

	private static boolean match(AttributeDesignatorKey a, AttributeDesignatorKey b){
		return a.getDataType().equals(b.getDataType())
				&& Objects.equals(a.getAttributeId(), b.getAttributeId())
				&& Objects.equals(a.getCategory(), b.getCategory()) &&
				(a.getIssuer() != null?Objects.equals(a.getIssuer(), b.getIssuer()):true);
	}
	@Override
	public Function<ResolverContext, Optional<AttributeSet>> getResolverFunction() {
		return resolver;
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

	public String toString(){
		return MoreObjects.toStringHelper(this)
		                  .add("issuer", issuer)
		                  .add("attributesById", attributesById)
		                  .add("resolver", resolver)
		                  .toString();
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

	public final static Builder builder(String id, String name, CategoryId category){
		return builder(id, name, null, category);
	}

	public final static Builder builder(String id, String name, String issuer, CategoryId category){
		return new Builder(id, name, issuer, category);
	}

	public final static class Builder
			extends BaseBuilder<AttributeSet, Builder>
	{
		private static Logger LOG = LoggerFactory.getLogger(Builder.class);

		private Map<String, AttributeDescriptor> attributesById;
		private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
		private String issuer;

		private Builder(
				String id,
				String name,
				String issuer,
				CategoryId categoryId){
			super(id, name, categoryId);
			this.issuer = Strings.emptyToNull(issuer);
			this.attributesById = new HashMap<>();
			this.attributesByKey = new HashMap<>();
		}

		public Builder  attribute(
				String attributeId,
				ValueType dataType,
				Collection<String> aliases){
			AttributeDescriptor d = AttributeDescriptor.of(attributeId, dataType, aliases);
			LOG.debug("Adding attributeId=\"{}\" aliases=\"{}\"", attributeId, aliases);
			if(attributesById.containsKey(attributeId)){
				throw SyntaxException.invalidResolverAttributeId(attributeId,
				                                                 "Builder already has an attribute with id=\"%s\" aliases={}",
				                                                 aliases);
			}
			AttributeDesignatorKey k = AttributeDesignatorKey.builder()
			                                                 .category(categoryId)
			                                                 .attributeId(attributeId)
			                                                 .dataType(dataType)
			                                                 .issuer(issuer)
			                                                 .build();
			attributesById.put(attributeId, d);
			attributesByKey.put(k, d);
			for(String alias : aliases ){
				if(attributesById.containsKey(alias)){
					throw SyntaxException.invalidResolverAttributeId(id,
							"Builder already has an attribute with id=\"%s\" aliases={}",
							                                         attributeId, aliases);
				}
				k = AttributeDesignatorKey.builder()
				                                                 .category(categoryId)
				                                                 .attributeId(alias)
				                                                 .dataType(dataType)
				                                                 .issuer(issuer)
				                                                 .build();
				if(this.attributesByKey.containsKey(k)){
					throw SyntaxException
							.invalidResolverAttributeId(id,
							                            "Builder already has an attribute with key=\"%s\" aliases={}",
							                            k, k);
				}
				attributesById.put(alias, d);
				attributesByKey.put(k, d);
			}
			return this;
		}

		public Builder  attribute(
				String attributeId,
				ValueType dataType, String ... aliases){
			return attribute(attributeId, dataType,
			                 (aliases == null)? Collections.emptyList(): Arrays.asList(aliases));
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeResolverDescriptor build(Function<ResolverContext, Map<String, BagOfValues>> resolverFunction){
			return new AttributeResolverDescriptor(this, resolverFunction);
		}
	}
}
