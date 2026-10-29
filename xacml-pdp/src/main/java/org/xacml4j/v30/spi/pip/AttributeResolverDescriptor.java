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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.ValueType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;


public final class AttributeResolverDescriptor
	extends BaseResolverDescriptor<AttributeSet>
{
	private final static Logger LOG = LoggerFactory.getLogger(AttributeResolverDescriptor.class);
	private String issuer;
	private Map<String, AttributeDescriptor> attributesById;
	private Map<AttributeDesignatorKey, AttributeDescriptor> attributesByKey;
	private Map<String, Function<ResolverContext, BagOfValues>> attributeResolvers;
	private Function<ResolverContext, Optional<AttributeSet>> resolver;

	private AttributeResolverDescriptor(Builder b){
		super(b);
		this.issuer = b.issuer;
		this.attributesByKey = ImmutableMap.copyOf(b.attributesByKey);
		this.attributesById = ImmutableMap.copyOf(b.attributesById);
		this.attributeResolvers = ImmutableMap.copyOf(b.attributeResolvers);
		this.resolver = this::resolveOnCallingThread;
		for(AttributeReferenceKey k : attributesByKey.keySet()){
			if(getContextKeyRefs().contains(k)){
				throw SyntaxException.invalidResolverReferenceSelfRef(getId(), "key={}", k);
			}
		}
	}

	private AttributeResolverDescriptor(Builder b, Function<ResolverContext,
			Map<String, BagOfValues>> resolverFunction){
		this(b);
		this.resolver = decorate(Objects.requireNonNull(resolverFunction, "resolverFunction"));
	}

	private Function<ResolverContext, Optional<AttributeSet>> decorate(Function<ResolverContext,
			Map<String, BagOfValues>> resolverFunction)
	{
		return (context)->{
			try{
				Map<String, BagOfValues> v = resolverFunction.apply(context);
				return Optional.ofNullable(v)
				               .flatMap(values->values.isEmpty()?Optional.empty():
				                                Optional.of(AttributeSet
						                                            .builder(this)
						                                            .clock(context.getClock())
						                                            .attributes(values).build()));
			}catch (EvaluationException e){
				LOG.debug(e.getMessage(), e);
				throw e;
			}
			catch (Exception e){
				LOG.debug(e.getMessage(), e);
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
	 * Tells whether this resolver has a function per attribute, which the information point
	 * calls in parallel (design D26)
	 */
	boolean isParallel(){
		return !attributeResolvers.isEmpty();
	}

	/**
	 * Calls the attribute functions of this parallel resolver through the given runner and
	 * combines their bags into one set (design D26). An attribute whose function gave no bag is
	 * not in the set, as for a resolver that leaves an attribute out of its map. The failure of a
	 * function, or a function that did not end in time, is kept for its attribute only.
	 *
	 * @param context the context of this call, prepared here for the functions
	 * @param runner the runner of the functions
	 * @return the set, or nothing when no function gave a bag and none failed
	 */
	Optional<AttributeSet> resolve(ResolverContext context, StructuredResolverRunner runner){
		ResolverContext prepared = new PreparedContext(context);
		List<Callable<BagOfValues>> tasks = new ArrayList<>(attributeResolvers.size());
		for(Function<ResolverContext, BagOfValues> f : attributeResolvers.values()){
			tasks.add(()->f.apply(prepared));
		}
		List<Future<BagOfValues>> outcomes = runner.runAll(tasks);
		if(givesNothing(outcomes)){
			return Optional.empty();
		}
		AttributeSet.Builder set = AttributeSet.builder(this).clock(prepared.getClock());
		int i = 0;
		for(String attributeId : attributeResolvers.keySet()){
			Future<BagOfValues> outcome = outcomes.get(i++);
			try{
				switch(outcome.state()){
					case SUCCESS -> {
						BagOfValues v = outcome.resultNow();
						if(v != null){
							set.attribute(attributeId, v);
						}
					}
					case FAILED -> set.failure(attributeId, outcome.exceptionNow());
					default -> set.failure(attributeId, new TimeoutException(String.format(
							"Attribute=\"%s\" of resolver=\"%s\" is not resolved in time", attributeId, getId())));
				}
			}catch(IllegalArgumentException e){
				// A bag of another type than the attribute's
				set.failure(attributeId, e);
			}
		}
		return Optional.of(set.build());
	}

	/**
	 * Tells whether every attribute function ended without a bag, which is no value from this
	 * resolver, as an empty map is from a synchronous one
	 */
	private static boolean givesNothing(List<Future<BagOfValues>> outcomes){
		for(Future<BagOfValues> outcome : outcomes){
			if(outcome.state() != Future.State.SUCCESS || outcome.resultNow() != null){
				return false;
			}
		}
		return true;
	}

	/**
	 * The resolver function of a parallel resolver: its attribute functions run on the calling
	 * thread, and the failure of any of them is the failure of the call, as for any resolver
	 */
	private Optional<AttributeSet> resolveOnCallingThread(ResolverContext context){
		Optional<AttributeSet> set = resolve(context, StructuredResolverRunner.DIRECT);
		Optional<Throwable> failure = set.flatMap(AttributeSet::firstFailure);
		if(failure.isPresent()){
			Throwable t = failure.get();
			if(t instanceof EvaluationException e){
				throw e;
			}
			if(t instanceof Error e){
				throw e;
			}
			throw new EvaluationException(context.getEvaluationContext(), t);
		}
		return set;
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

	/**
	 * The context that the attribute functions of a parallel resolver read. It is prepared on the
	 * thread that resolves the attribute: a function, which may run on another thread, reads the
	 * context keys resolved there and resolves nothing itself, so the evaluation context is not
	 * given to it (designs D26, D27).
	 */
	private static final class PreparedContext implements ResolverContext
	{
		private final ResolverContext context;
		private final Map<AttributeReferenceKey, BagOfValues> keys;
		private final Clock clock;

		PreparedContext(ResolverContext context){
			this.context = context;
			this.keys = context.getResolvedKeys();
			this.clock = context.getClock();
		}

		@Override
		public Optional<Content.Type> getContentType() {
			return context.getContentType();
		}

		@Override
		public ZonedDateTime getCurrentDateTime() {
			return ZonedDateTime.now(clock);
		}

		@Override
		public Clock getClock() {
			return clock;
		}

		@Override
		public ResolverDescriptor getDescriptor() {
			return context.getDescriptor();
		}

		@Override
		public ResolverCacheKey getCacheKey() {
			// From the prepared values in their order, without a builder (cycle AL)
			return ResolverCacheKey.of(getDescriptor().getId(), keys.values());
		}

		@Override
		public Optional<BagOfValues> resolveContextRef(AttributeReferenceKey key) {
			return Optional.ofNullable(keys.get(key));
		}

		@Override
		public Map<AttributeReferenceKey, BagOfValues> resolveAllContextRefs() {
			return keys;
		}

		@Override
		public boolean isCacheable() {
			return context.isCacheable();
		}

		@Override
		public Map<AttributeReferenceKey, BagOfValues> getResolvedKeys() {
			return keys;
		}

		@Override
		public EvaluationContext getEvaluationContext() {
			throw new IllegalStateException(String.format(
					"The attribute functions of resolver=\"%s\" read their context keys, not the evaluation context",
					getDescriptor().getId()));
		}
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
		private Map<String, Function<ResolverContext, BagOfValues>> attributeResolvers;
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
			this.attributeResolvers = new LinkedHashMap<>();
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

		/**
		 * Adds an attribute with the function that resolves it, for a resolver built with
		 * {@link #buildParallel()} (design D26). A function that returns {@code null} gives no
		 * value for its attribute, so the next resolver is asked; an empty bag is a value. The
		 * function may run on another thread: it reads the context keys of the resolver from its
		 * context and does not get the evaluation context.
		 *
		 * @param attributeId an attribute identifier
		 * @param dataType the attribute data type
		 * @param resolver the function that resolves the attribute
		 * @return this builder
		 */
		public Builder attribute(
				String attributeId,
				ValueType dataType,
				Function<ResolverContext, BagOfValues> resolver){
			Objects.requireNonNull(resolver, "resolver");
			attribute(attributeId, dataType, Collections.emptyList());
			attributeResolvers.put(attributeId, resolver);
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeResolverDescriptor build(Function<ResolverContext, Map<String, BagOfValues>> resolverFunction){
			Preconditions.checkState(attributeResolvers.isEmpty(),
			                         "Resolver=\"%s\" has attribute functions: build it with buildParallel()", id);
			return new AttributeResolverDescriptor(this, resolverFunction);
		}

		/**
		 * Builds a resolver whose attribute functions the information point calls in parallel,
		 * within its time limit and its limit of concurrent resolutions (design D26). Every
		 * attribute must have its function.
		 *
		 * @return the resolver descriptor
		 */
		public AttributeResolverDescriptor buildParallel(){
			Preconditions.checkState(!attributeResolvers.isEmpty(),
			                         "Resolver=\"%s\" has no attribute function", id);
			for(AttributeDescriptor a : attributesById.values()){
				Preconditions.checkState(attributeResolvers.containsKey(a.getAttributeId()),
				                         "Attribute=\"%s\" of resolver=\"%s\" has no function", a.getAttributeId(), id);
			}
			return new AttributeResolverDescriptor(this);
		}
	}
}
