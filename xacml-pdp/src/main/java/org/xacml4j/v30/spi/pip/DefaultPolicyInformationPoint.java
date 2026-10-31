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


import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.spi.observe.DecisionObservation;

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
	private StructuredResolverRunner runner;
	/** The attribute sets of the decision this point is for; nothing for the point itself (task 30) */
	private Optional<DecisionSets> sets;


	DefaultPolicyInformationPoint(PolicyInformationPoint.Builder<?> b) {
		this.id = Preconditions.checkNotNull(b.id, "id");;
		this.cache = Preconditions.checkNotNull(b.cache, "cache");;
		this.registry = Preconditions.checkNotNull(b.registry, "registry");
		this.runner = new StructuredResolverRunner(
				b.resolverExecutor.orElseGet(Executors::newVirtualThreadPerTaskExecutor),
				b.maxConcurrentResolutions, b.resolverTimeout);
		this.sets = Optional.empty();
	}

	/** The point of one decision: the resolvers, cache, and runner of the given point, with the sets of the decision */
	private DefaultPolicyInformationPoint(DefaultPolicyInformationPoint pip) {
		this.id = pip.id;
		this.cache = pip.cache;
		this.registry = pip.registry;
		this.runner = pip.runner;
		this.sets = Optional.of(new DecisionSets());
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public PolicyInformationPoint forDecision() {
		return sets.isPresent()? this: new DefaultPolicyInformationPoint(this);
	}

	/** Answered by the registry, which the point of a decision shares with the point it is for */
	@Override
	public boolean hasNoResolver(AttributeReferenceKey key) {
		return registry.hasNoResolver(key);
	}

	/**
	 * Asks the matching resolvers in order; each one is a resolver span of the observation of the
	 * decision, outside the guard of the resolver (design D34)
	 */
	@Override
	public Optional<BagOfValues> resolve(
			final EvaluationContext context,
			AttributeDesignatorKey designatorKey)
	{
		Collection<AttributeResolverDescriptor> matched = registry.getMatchingAttributeResolver(context, designatorKey);
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		AttributeReferenceEvaluationException failure = null;
		for(AttributeResolverDescriptor d : matched){
			LOG.debug("Processing resolverId={} for key={}", d.getId(), designatorKey);
			boolean observed = o.isEnabled();
			Object parent = observed ? o.resolverStarted(context, d.getId(), designatorKey) : null;
			Optional<BagOfValues> r;
			try{
				r = resolve(o, d, context, designatorKey);
			}catch (Exception e){
				AttributeReferenceEvaluationException mapped = mapped(d.getId(), designatorKey, e);
				failure = (failure != null)?failure:mapped;
				if(observed){
					o.resolverFailed(context, d.getId(), designatorKey, parent, mapped);
				}
				continue;
			}catch (Error e){
				if(observed){
					o.resolverFailed(context, d.getId(), designatorKey, parent, e);
				}
				throw e;
			}
			if(observed){
				o.resolverFinished(context, d.getId(), designatorKey, parent, r);
			}
			if(r.isPresent()){
				return r;
			}
		}
		return emptyUnlessFailed(failure);
	}

	/** The answer of one resolver for the key */
	private Optional<BagOfValues> resolve(DecisionObservation o, AttributeResolverDescriptor d,
			EvaluationContext context, AttributeDesignatorKey designatorKey) throws Exception
	{
		ResolverContext resolverContext = ResolverContext.createContext(context, d);
		Optional<AttributeSet> value = resolverContext.isCacheable()?
				cached(o, d, context, resolverContext, designatorKey): invoke(d, resolverContext);
		LOG.debug("Resolved key={} via resolverId={} value={}", designatorKey, value, d.getId());
		if(value == null || !value.isPresent()){
			return Optional.empty();
		}
		Optional<BagOfValues> r = value.get().get(designatorKey.getAttributeId());
		LOG.debug("Resolved key={} value={} via resolverId={}", designatorKey, r, d.getId());
		if(!r.isPresent()){
			// A failed attribute function of a parallel resolver is a failure of this resolver (design D26)
			value.get().rethrowFailure(designatorKey.getAttributeId());
			// A set without this attribute is "no value" from this
			// resolver: the next resolver is asked, and an earlier
			// failure is not dropped.
		}
		return r;
	}

	/**
	 * Calls a resolver: a parallel one through the runner of this information point (design D26)
	 */
	private Optional<AttributeSet> invoke(AttributeResolverDescriptor d, ResolverContext context){
		return d.isParallel()? d.resolve(context, runner): d.getResolverFunction().apply(context);
	}

	/**
	 * The set of a cacheable resolver that has the attribute: within one decision, the set the
	 * decision saw for the same call (task 30); else the set in the cache; else the set the
	 * resolver gives now, which goes into the cache only after a miss (design D27) and only
	 * without a failed attribute (design D26), and which the decision keeps. The decision's set
	 * is consulted only when the key of the call is known from what the cache lookup or the
	 * resolver resolved, as a key built here would resolve the context keys of the resolver
	 * before it asks for them (review R-FABLE-sol): before the cache lookup when the key is known
	 * already, so a failing cache cannot take an answer the decision has, and after the lookup
	 * otherwise, as the lookup may have made the key known (review R-FABLE2-sol). The cache hit
	 * is reported only when the value of the cache is the set that serves (design D34): a set
	 * that a sibling lookup of the decision kept first serves instead of the cache's value, and
	 * is no hit (review R-FABLE3-sol).
	 */
	private Optional<AttributeSet> cached(DecisionObservation o, AttributeResolverDescriptor d,
			EvaluationContext evaluationContext, ResolverContext context, AttributeDesignatorKey key){
		String attributeId = key.getAttributeId();
		boolean keyKnown = hasKey(context);
		Optional<AttributeSet> seen = keyKnown? seen(context, attributeId): Optional.empty();
		if(seen.isPresent()){
			return seen;
		}
		Optional<AttributeSet> value = cache.getAttributes(context);
		if(!keyKnown){
			seen = seen(context, attributeId);
			if(seen.isPresent()){
				return seen;
			}
		}
		if(value.isPresent()){
			AttributeSet candidate = value.get();
			AttributeSet selected = remember(context, candidate, attributeId);
			if(selected == candidate){
				o.resolverCacheHit(evaluationContext, d.getId(), key);
			}
			return Optional.of(selected);
		}
		value = invoke(d, context);
		if(value == null || !value.isPresent() || !value.get().isComplete()){
			return value;
		}
		cache.putAttributes(context, value.get());
		return Optional.of(remember(context, value.get(), attributeId));
	}

	/** The set this decision saw for the call, when the key of the call is known */
	private Optional<AttributeSet> seen(ResolverContext context, String attributeId){
		return sets.isPresent() && hasKey(context)? sets.get().get(context, attributeId): Optional.empty();
	}

	/** Keeps the set for this decision when the key of the call is known, and returns the set that serves */
	private AttributeSet remember(ResolverContext context, AttributeSet set, String attributeId){
		return sets.isPresent() && hasKey(context)? sets.get().put(context, set, attributeId): set;
	}

	/** Whether the key of the call is known without resolving a context key of the resolver */
	private static boolean hasKey(ResolverContext context){
		return context instanceof ResolverContext.Default own && own.isCacheKeyAvailable();
	}

	/**
	 * The attribute sets that one decision saw, by the key of the resolver call that gave them:
	 * the resolver and the values of its context keys, as {@link ResolverCacheKey} (task 30 of
	 * change optimize-pdp-evaluation, design D27). A set serves an attribute of its resolver that
	 * the decision demands later, while the set is within the time to live its resolver declared;
	 * an attribute that is not in the set is resolved as if there were no set, and nothing is kept
	 * of a failure. The callers ask for the key of a call only when it is known.
	 */
	private static final class DecisionSets
	{
		private final Map<ResolverCacheKey, AttributeSet> sets = new ConcurrentHashMap<>();

		Optional<AttributeSet> get(ResolverContext context, String attributeId){
			ResolverCacheKey key = context.getCacheKey();
			AttributeSet set = sets.get(key);
			if(set == null){
				return Optional.empty();
			}
			if(expired(set, context)){
				sets.remove(key, set);
				return Optional.empty();
			}
			return set.get(attributeId).isPresent()? Optional.of(set): Optional.empty();
		}

		/**
		 * Keeps the set unless the decision has one for the call already. The kept set serves
		 * when it is within its time to live and has the attribute, so that two calls of the
		 * decision in flight at the same time answer from one set (review R-FABLE-sol); an
		 * expired set gives way to the given one, and an attribute missing from the kept set is
		 * served by the given one, as a miss. When another call replaced or removed the expired
		 * set first, the kept set is selected again, so that two refreshes in flight at the same
		 * time answer from the one that won (review R-FABLE2-sol). Only map operations run here,
		 * never a resolver.
		 */
		AttributeSet put(ResolverContext context, AttributeSet set, String attributeId){
			ResolverCacheKey key = context.getCacheKey();
			AttributeSet kept = sets.putIfAbsent(key, set);
			while(kept != null){
				if(!expired(kept, context)){
					return kept.get(attributeId).isPresent()? kept: set;
				}
				if(sets.replace(key, kept, set)){
					return set;
				}
				kept = sets.putIfAbsent(key, set);
			}
			return set;
		}

		private static boolean expired(AttributeSet set, ResolverContext context){
			Duration age = Duration.between(set.getTimestamp(), context.getClock().instant());
			return age.compareTo(set.getDescriptor().getPreferredCacheTTL()) > 0;
		}
	}

	/**
	 * Maps the failure of a resolver; the callers keep the first one. Nothing
	 * is called on the exception of the resolver before it is mapped, and it
	 * is not given to the logger: an exception from its {@code getMessage()}
	 * must not replace the mapped failure.
	 */
	private static AttributeReferenceEvaluationException mapped(
			String resolverId, AttributeReferenceKey key, Exception e)
	{
		AttributeReferenceEvaluationException mapped = AttributeReferenceEvaluationException
				.forResolverFailure(key, e);
		LOG.debug("resolverId={} failed: {}", resolverId, mapped.getMessage());
		return mapped;
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
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		AttributeReferenceEvaluationException failure = null;
		for(ContentResolverDescriptor d : matched)
		{
			LOG.debug("Processing resolverId={} for key={}", d.getId(), selectorKey);
			boolean observed = o.isEnabled();
			Object parent = observed ? o.resolverStarted(context, d.getId(), selectorKey) : null;
			Optional<ContentRef> value;
			try
			{
				value = content(o, d, context, selectorKey);
				LOG.debug("Resolved key={} content={}", selectorKey, value);
			}catch (Exception e){
				// A failure of the resolver, whatever exception type and
				// status it chose.
				AttributeReferenceEvaluationException mapped = mapped(d.getId(), selectorKey, e);
				failure = (failure != null)?failure:mapped;
				if(observed){
					o.resolverFailed(context, d.getId(), selectorKey, parent, mapped);
				}
				continue;
			}catch (Error e){
				if(observed){
					o.resolverFailed(context, d.getId(), selectorKey, parent, e);
				}
				throw e;
			}
			Optional<BagOfValues> r;
			try{
				// The selector path is evaluated on the supplied content: a path error is thrown
				// at once, not asked of the next resolver, mapped as the resolver failure the
				// handler makes of every exception of this point, so that the resolver span and
				// the attribute get one exception (review R-D34-argon).
				r = (value != null && value.isPresent())?value.get().getContent().resolve(selectorKey):Optional.empty();
			}catch (Exception e){
				AttributeReferenceEvaluationException mapped = mapped(d.getId(), selectorKey, e);
				if(observed){
					o.resolverFailed(context, d.getId(), selectorKey, parent, mapped);
				}
				throw mapped;
			}catch (Error e){
				if(observed){
					o.resolverFailed(context, d.getId(), selectorKey, parent, e);
				}
				throw e;
			}
			LOG.debug("Resolved key={} value={} via resolverId={}", selectorKey, r, d.getId());
			if(observed){
				o.resolverFinished(context, d.getId(), selectorKey, parent, r);
			}
			if(r.isPresent()){
				return r;
			}
			// Content without a selected node is "no value" from this
			// resolver: the next resolver is asked, and an earlier failure
			// is not dropped.
		}
		return emptyUnlessFailed(failure);
	}

	/** The content of one resolver: from the cache of this point, else from the resolver */
	private Optional<ContentRef> content(DecisionObservation o, ContentResolverDescriptor d,
			EvaluationContext context, AttributeSelectorKey selectorKey)
	{
		ResolverContext resolverContext = ResolverContext.createContext(context, d);
		if(!resolverContext.isCacheable()){
			return d.getResolverFunction().apply(resolverContext);
		}
		Optional<ContentRef> value = cache.getContent(resolverContext);
		if(value.isPresent()){
			o.resolverCacheHit(context, d.getId(), selectorKey);
			return value;
		}
		value = d.getResolverFunction().apply(resolverContext);
		if(value != null && value.isPresent()){
			// Into the cache only after a miss (design D27)
			cache.putContent(resolverContext, value.get());
		}
		return value;
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
