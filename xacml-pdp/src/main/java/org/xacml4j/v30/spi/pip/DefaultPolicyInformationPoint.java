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

import static org.xacml4j.util.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;

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
		this.id = checkNotNull(b.id, "id");;
		this.cache = checkNotNull(b.cache, "cache");;
		this.registry = checkNotNull(b.registry, "registry");
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
					return r;
				}
			}catch (Exception e){
				LOG.debug(e.getMessage(), e);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<BagOfValues> resolve(EvaluationContext context,
	                                 AttributeSelectorKey selectorKey)
	{
		Collection<ContentResolverDescriptor> matched = registry.getMatchingContentResolver(context, selectorKey);
		for(ContentResolverDescriptor d : matched)
		{
			LOG.debug("Processing resolverId={} for key={}", d.getId(), selectorKey);
			ResolverContext resolverContext = ResolverContext.createContext(context, d);
			try
			{
				Optional<ContentRef> value = resolverContext.isCacheable()?
				                             cache.getContent(resolverContext)
				                                  .or(()->d.getResolverFunction().apply(resolverContext)):
				                             d.getResolverFunction().apply(resolverContext);
				LOG.debug("Resolved key={} content={}", selectorKey, value);
				if(value != null && value.isPresent()){
					if(resolverContext.isCacheable()){
						LOG.debug("Caching key={} content={}", selectorKey, value);
						cache.putContent(resolverContext, value.get());
					}
					Optional<BagOfValues> r = value.flatMap(c->c.getContent()
					                         .resolve(selectorKey));
					LOG.debug("Resolved key={} value={} via resolverId={}", selectorKey, r, d.getId());
					return r;
				}
			}catch (Exception e){
				LOG.debug("Error while resolverId={} key={}", d.getId(), selectorKey, e);
			}
		}
		return Optional.empty();

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
