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
import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * A default implementation of {@link PolicyInformationPoint}
 *
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyInformationPoint
		implements PolicyInformationPoint {
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);

	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;

	public DefaultPolicyInformationPoint(String id,
	                                     ResolverRegistry resolvers,
	                                     PolicyInformationPointCacheProvider cache) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(resolvers);
		Preconditions.checkNotNull(cache);
		this.id = id;
		this.cache = cache;
		this.registry = resolvers;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public BagOfAttributeExp resolve(
			final DecisionRuleEvaluationContext context,
			AttributeDesignatorKey ref) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Trying to resolve designator=\"{}\"", ref);
		}
		Iterable<AttributeResolver> resolvers = registry.getMatchingAttributeResolvers(context, ref);
		if (Iterables.isEmpty(resolvers)) {
			if (log.isDebugEnabled()) {
				log.debug("No matching resolver found for designator=\"{}\"", ref);
			}
			return null;
		}
		for (AttributeResolver r : resolvers) {
			AttributeResolverDescriptor d = r.getDescriptor();
			Preconditions.checkState(d.canResolve(ref));
			ResolverContext rContext = createContext(context, d);
			AttributeSet attributes = null;
			if (d.isCacheable()) {
				if (log.isDebugEnabled()) {
					log.debug("Trying to find resolver id=\"{}\" values in cache", d.getId());
				}
				attributes = cache.getAttributes(rContext);
				if (attributes != null &&
						!isExpired(attributes, context)) {
					if (log.isDebugEnabled()) {
						log.debug("Found cached resolver id=\"{}\" values=\"{}\"",
								d.getId(), attributes);
					}
					return attributes.get(ref.getAttributeId());
				}
			}
			try {
				if (log.isDebugEnabled()) {
					log.debug("Trying to resolve values with resolver id=\"{}\"",
							d.getId());
				}
				attributes = r.resolve(rContext);
				if (attributes.isEmpty()) {
					if (log.isDebugEnabled()) {
						log.debug("Resolver id=\"{}\" failed to resolve attribute",
								d.getId());
					}
					continue;
				}
				if (log.isDebugEnabled()) {
					log.debug("Resolved attribute=\"{}\"",
							attributes);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Resolver id=\"{}\" failed to resolve attribute", d.getId(), e);
				}
				continue;
			}
			// check if resolver
			// descriptor allows long term caching
			if (d.isCacheable() &&
					!attributes.isEmpty()) {
				cache.putAttributes(rContext, attributes);
			}
			context.setDecisionCacheTTL(d.getPreferredCacheTTL());
			return attributes.get(ref.getAttributeId());
		}
		return ref.getDataType().emptyBag();
	}

	private boolean isExpired(AttributeSet v, DecisionRuleEvaluationContext context) {
		return ((context.getTicker().read() - v.getCreatedTime()) /
				1000000000L) >= v.getDescriptor().getPreferredCacheTTL();
	}

	private boolean isExpired(Content v, DecisionRuleEvaluationContext context) {
		long duration = context.getTicker().read() - v.getTimestamp() / 1000000000L;

		if (log.isDebugEnabled()) {
			log.debug("Attribute set=\"{}\" age=\"{}\" in cache", v, duration);
		}
		return duration >= v.getDescriptor().getPreferredCacheTTL();
	}

	@Override
	public Node resolve(final DecisionRuleEvaluationContext context,
	                    CategoryId category)
			throws Exception {
		ContentResolver r = registry.getMatchingContentResolver(context, category);
		if (r == null) {
			return null;
		}
		ContentResolverDescriptor d = r.getDescriptor();
		ResolverContext pipContext = createContext(context, d);
		Content v = null;
		if (d.isCacheable()) {
			v = cache.getContent(pipContext);
			if (v != null &&
					!isExpired(v, context)) {
				if (log.isDebugEnabled()) {
					log.debug("Found cached content=\"{}\"", v);
				}
				return v.getContent();
			}
		}
		try {
			v = r.resolve(pipContext);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Received error while resolving content for category=\"{}\"",
						category, e);
			}
			return null;
		}
		if (d.isCacheable()) {
			cache.putContent(pipContext, v);
		}
		context.setDecisionCacheTTL(d.getPreferredCacheTTL());
		return v.getContent();
	}

	@Override
	public ResolverRegistry getRegistry() {
		return registry;
	}

	private ResolverContext createContext(DecisionRuleEvaluationContext context, ResolverDescriptor d)
			throws EvaluationException {
		return new DefaultResolverContext(context, d);
	}
}
