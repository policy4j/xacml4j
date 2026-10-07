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


import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;

import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyInformationPoint}
 *
 * @author Giedrius Trumpickas
 */
final class DefaultPolicyInformationPoint
		implements PolicyInformationPoint
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;


	DefaultPolicyInformationPoint(PolicyInformationPoint.Builder b) {
		this.id = Preconditions.checkNotNull(b.id, "id");;
		this.cache = Preconditions.checkNotNull(b.cache, "cache");;
		this.registry = Preconditions.checkNotNull(b.registry, "registry");
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Optional<BagOfValues> resolve(
			final EvaluationContext context,
			AttributeDesignatorKey designatorKey)
	{
		Collection<AttributeResolverDescriptor> matched = registry.getMatchingAttributeResolver(context, designatorKey);
		AttributeReferenceEvaluationException failure = null;
		for(AttributeResolverDescriptor d : matched){
			LOG.debug("Processing resolverId={} for key={}", d.getId(), designatorKey);
			try{
				ResolverContext resolverContext = ResolverContext.createContext(context, d);
				Optional<AttributeSet> value = resolverContext.isCacheable()?
				                               cache.getAttributes(resolverContext)
				                                    .or(()->d.getResolverFunction().apply(resolverContext)):
				                               d.getResolverFunction().apply(resolverContext);
				LOG.debug("Resolved key={} via resolverId={} value={}", designatorKey, value, d.getId());
				if(value != null && value.isPresent()){
					if(resolverContext.isCacheable()){
						LOG.debug("Caching key={}  attributeSet={}", designatorKey, value);
						cache.putAttributes(resolverContext, value.get());
					}

					Optional<BagOfValues> r = value.flatMap(v->v.get(designatorKey
							                              .getAttributeId()));
					LOG.debug("Resolved key={} value={} via resolverId={}", designatorKey, r, d.getId());
					if(r.isPresent()){
						return r;
					}
					// A set without this attribute is "no value" from this
					// resolver: the next resolver is asked, and an earlier
					// failure is not dropped.
				}
			}catch (Exception e){
				failure = firstFailure(failure, d.getId(), designatorKey, e);
			}
		}
		return emptyUnlessFailed(failure);
	}

	/**
	 * Maps the failure of a resolver and keeps the first one. Nothing is
	 * called on the exception of the resolver before it is mapped, and it is
	 * not given to the logger: an exception from its {@code getMessage()}
	 * must not replace the mapped failure.
	 */
	private static AttributeReferenceEvaluationException firstFailure(
			AttributeReferenceEvaluationException first,
			String resolverId, AttributeReferenceKey key, Exception e)
	{
		AttributeReferenceEvaluationException mapped = AttributeReferenceEvaluationException
				.forResolverFailure(key, e);
		LOG.debug("resolverId={} failed: {}", resolverId, mapped.getMessage());
		return first != null?first:mapped;
	}

	/**
	 * Reports "no value" only when no resolver failed. The matching resolvers
	 * are asked in registration order until one yields a value for the
	 * reference. When none did and at least one failed, the first failure is
	 * thrown, so a retrieval error is not mistaken for a missing attribute.
	 * The failure is a processing error with a fixed status message: the
	 * status and the message that a resolver chose are not passed on.
	 */
	private static Optional<BagOfValues> emptyUnlessFailed(AttributeReferenceEvaluationException failure)
	{
		if(failure != null){
			throw failure;
		}
		return Optional.empty();
	}

	@Override
	public Optional<BagOfValues> resolve(EvaluationContext context,
	                                 AttributeSelectorKey selectorKey)
	{
		Collection<ContentResolverDescriptor> matched = registry.getMatchingContentResolver(context, selectorKey);
		AttributeReferenceEvaluationException failure = null;
		for(ContentResolverDescriptor d : matched)
		{
			LOG.debug("Processing resolverId={} for key={}", d.getId(), selectorKey);
			Optional<ContentRef> value;
			try
			{
				ResolverContext resolverContext = ResolverContext.createContext(context, d);
				value = resolverContext.isCacheable()?
				        cache.getContent(resolverContext)
				             .or(()->d.getResolverFunction().apply(resolverContext)):
				        d.getResolverFunction().apply(resolverContext);
				LOG.debug("Resolved key={} content={}", selectorKey, value);
				if(value != null && value.isPresent() && resolverContext.isCacheable()){
					LOG.debug("Caching key={} content={}", selectorKey, value);
					cache.putContent(resolverContext, value.get());
				}
			}catch (Exception e){
				// A failure of the resolver, whatever exception type and
				// status it chose.
				failure = firstFailure(failure, d.getId(), selectorKey, e);
				continue;
			}
			if(value != null && value.isPresent()){
				// The selector path is evaluated on the supplied content outside
				// the resolver boundary: a path error is thrown at once and is
				// not the failure of one resolver.
				Optional<BagOfValues> r = value.flatMap(c->c.getContent()
				                         .resolve(selectorKey));
				LOG.debug("Resolved key={} value={} via resolverId={}", selectorKey, r, d.getId());
				if(r.isPresent()){
					return r;
				}
				// Content without a selected node is "no value" from this
				// resolver: the next resolver is asked, and an earlier failure
				// is not dropped.
			}
		}
		return emptyUnlessFailed(failure);
	}

	public static Builder builder(String id){
		return new Builder(id);
	}

	public static final class Builder
			extends PolicyInformationPoint.Builder<Builder>
	{
		public Builder(String id) {
			super(id);
		}

		@Override
		public Builder getThis() {
			return this;
		}
	}
}
