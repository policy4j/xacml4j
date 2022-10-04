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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;

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
	public Optional<BagOfValues> resolve(
			final EvaluationContext context,
			AttributeDesignatorKey designatorKey) throws Exception
	{
		Iterable<AttributeResolverDescriptor> matched = registry.getMatchingAttributeResolver(context, designatorKey);
		return attributeStrategy.resolve(context, matched, (resolverContext)->cache.getAttributes(resolverContext))
				.flatMap(a->a.get(designatorKey.getAttributeId()));
	}

	@Override
	public Optional<Content> resolve(EvaluationContext context,
	                                 AttributeSelectorKey selectorKey)
	{
		Iterable<ContentResolverDescriptor> matched = registry.getMatchingContentResolver(context, selectorKey);
		return contentStrategy.resolve(context, matched, (resolverContext)->cache.getContent(resolverContext))
				.map(v->v.getContent());
	}
}
