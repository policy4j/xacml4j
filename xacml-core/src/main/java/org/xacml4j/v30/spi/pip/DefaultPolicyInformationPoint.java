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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;
import org.xacml4j.v30.BagOfAttributeValues;


import java.util.Optional;
import static org.xacml4j.util.Preconditions.checkNotNull;

/**
 * A default implementation of {@link PolicyInformationPoint}
 *
 * @author Giedrius Trumpickas
 */
final class DefaultPolicyInformationPoint
		implements PolicyInformationPoint {
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);

	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;
	private ResolutionStrategy contentStrategy = ResolutionStrategy.FIRST_NON_EMPTY_IGNORE_ERROR;
	private ResolutionStrategy attributeStrategy = ResolutionStrategy.FIRST_NON_EMPTY_IGNORE_ERROR;

	DefaultPolicyInformationPoint(String id,
	                                     ResolverRegistry registry,
	                                     ResolutionStrategy contentStrategy,
								  		ResolutionStrategy attributeStrategy,
	                                     PolicyInformationPointCacheProvider cache) {
		this.id = checkNotNull(id, "id");;
		this.cache = checkNotNull(cache, "cache");;
		this.registry = checkNotNull(registry, "registry");
		this.contentStrategy = checkNotNull(contentStrategy, "contentStrategy");
		this.attributeStrategy = checkNotNull(attributeStrategy, "attributeStrategy");
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Optional<BagOfAttributeValues> resolve(
			final EvaluationContext context,
			AttributeDesignatorKey designatorKey) throws Exception
	{
		Iterable<Resolver<AttributeSet>> matched = registry.getMatchingAttributeResolver(context, designatorKey);
		Optional<AttributeSet> v = attributeStrategy.resolve(
				matched, (r) -> resolveAttributes(createContext(context, r)));
		return v.flatMap(a->a.get(designatorKey.getAttributeId()));
	}

	@Override
	public Optional<Content> resolve(EvaluationContext context,
	                    AttributeSelectorKey selectorKey)
	{
		Iterable<Resolver<ContentRef>> matched = registry.getMatchingContentResolver(context, selectorKey);
		Optional<ContentRef> v = contentStrategy.resolve(
				matched, (r) -> resolveContent(createContext(context, r)));
		return v.map(c->c.getContent());
	}



	private Optional<ContentRef> resolveContent(ResolverContext resolverContext){
		Resolver<ContentRef> resolver = resolverContext.getResolver();
		Optional<ContentRef> fromCache =
				resolverContext.isCacheable()?
						cache.getContent(resolverContext)
						:Optional.empty();
		return fromCache
				.or(()->resolver.resolve(resolverContext));

	}

	private Optional<AttributeSet> resolveAttributes(ResolverContext resolverContext){
		Resolver<AttributeSet> resolver = resolverContext.getResolver();
		Optional<AttributeSet> fromCache =
				resolverContext.isCacheable()?
						cache.getAttributes(resolverContext)
						:Optional.empty();
		return fromCache
				.or(()->resolver.resolve(resolverContext));
	}

	private ResolverContext createContext(EvaluationContext context, Resolver<?> resolver)
	{
		return new DefaultResolverContext(resolver, context);
	}
}
