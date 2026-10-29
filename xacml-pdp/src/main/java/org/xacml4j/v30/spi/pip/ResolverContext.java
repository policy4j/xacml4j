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
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableMap;


/**
 * The context of one call of a resolver. It belongs to that call: a resolver that hands work to
 * other threads reads its dependencies here, on the calling thread, and passes the values on
 * (design D27 of optimize-pdp-evaluation).
 */
public interface ResolverContext
{
	/**
	 * Gets optionally requested content type
	 *
	 * @return {@link Optional< Content.Type>/>}
	 */
	Optional<Content.Type> getContentType();

	/**
	 * Gets current evaluation context date/time
	 *
	 * @return {@link Calendar} defaultProvider
	 * representing current date/time
	 */
	ZonedDateTime getCurrentDateTime();

	/**
	 * Gets ticker
	 *
	 * @return {@link Ticker}
	 */
	Clock getClock();

	/**
	 * Gets resolver descriptor
	 *
	 * @return {@link ResolverDescriptor}
	 */
	ResolverDescriptor getDescriptor();

	ResolverCacheKey getCacheKey();

	/**
	 * Resolves context reference key required
	 * for resolver to resolve its own attributes
	 */
	Optional<BagOfValues> resolveContextRef(AttributeReferenceKey key);
	Map<AttributeReferenceKey, BagOfValues> resolveAllContextRefs();

	boolean isCacheable();

	/**
	 * Gets resolved keys, key resolution implies
	 * that keys were used to look up attributes
	 * from the back end sources
	 */
	Map<AttributeReferenceKey, BagOfValues> getResolvedKeys();

	EvaluationContext getEvaluationContext();

	static ResolverContext createContext(EvaluationContext context, ResolverDescriptor resolver){
		return new Default(context, resolver);
	}

	final class Default implements
			ResolverContext
	{
		private final Optional<Content.Type> contentType;
		private final EvaluationContext context;
		private final ResolverDescriptor resolver;
		// Resolved when first asked for; a descriptor whose context keys all resolved to nothing
		// asks again on the next call, as before (design D27 keeps that retry)
		private Map<AttributeReferenceKey, BagOfValues> resolvedKeys;
		private ResolverCacheKey cacheKey;

		public Default(
				EvaluationContext context,
				ResolverDescriptor resolver,
				Supplier<Optional<Content.Type>> supplier) throws EvaluationException {
			this.resolver = Objects.requireNonNull(resolver, "resolver");
			this.context = Objects.requireNonNull(context, "context");
			this.contentType = Objects.requireNonNull(supplier, "contentType").get();
		}

		public Default(EvaluationContext context, ResolverDescriptor resolver){
			this(context, resolver, ()->Optional.empty());
		}

		public EvaluationContext getEvaluationContext(){
			return context;
		}

		@Override
		public Optional<Content.Type> getContentType() {
			return  contentType;
		}

		@Override
		public ZonedDateTime getCurrentDateTime() {
			return context.getCurrentDateTime();
		}

		@Override
		public Clock getClock(){
			return context.getClock();
		}

		public ResolverCacheKey getCacheKey(){
			if(cacheKey == null){
				if(resolvedKeys == null ||
						resolvedKeys.isEmpty()){
					this.resolvedKeys = resolveAllContextRefs();
				}
				// From the resolved values in their order, without a builder (cycle AL)
				this.cacheKey = ResolverCacheKey.of(getDescriptor().getId(), resolvedKeys.values());
			}
			return cacheKey;
		}

		/**
		 * Whether {@link #getCacheKey()} answers from what this context resolved already: the key
		 * is built, the context keys are resolved, or the descriptor declares none. Otherwise the
		 * key would resolve the context keys before the resolver asks for them, and the resolver
		 * would ask again when they all resolved to nothing (the timing design D27 keeps; the
		 * sets of a decision are kept only with a known key, review R-FABLE-sol).
		 */
		boolean isCacheKeyAvailable(){
			return cacheKey != null
					|| (resolvedKeys != null && !resolvedKeys.isEmpty())
					|| getDescriptor().getContextKeyRefs().isEmpty();
		}

		@Override
		public ResolverDescriptor getDescriptor(){
			return resolver;
		}

		public Map<AttributeReferenceKey, BagOfValues> getResolvedKeys(){
			return resolvedKeys != null? resolvedKeys: resolveAllContextRefs();
		}

		@Override
		public Optional<BagOfValues> resolveContextRef(AttributeReferenceKey key)
		{
			if(resolvedKeys == null || resolvedKeys.isEmpty()){
				this.resolvedKeys = resolveAllContextRefs();
			}
			return Optional.ofNullable(resolvedKeys.get(key));
		}

		/**
		 * Resolves the context keys of the descriptor in their declared order and keeps the
		 * present ones in that order; one context key, the common case, makes its map directly
		 * (cycle AL)
		 */
		@Override
		public Map<AttributeReferenceKey, BagOfValues> resolveAllContextRefs(){
			List<AttributeReferenceKey> keys = getDescriptor().getContextKeyRefs();
			this.resolvedKeys = keys.size() == 1? resolved(keys.get(0)): resolved(keys);
			return resolvedKeys;
		}

		/** The map of one context key: its value when it has one, else nothing */
		private Map<AttributeReferenceKey, BagOfValues> resolved(AttributeReferenceKey key){
			Optional<BagOfValues> value = context.resolve(key);
			return value.isPresent()? ImmutableMap.of(key, value.get()): ImmutableMap.of();
		}

		/** The map of several context keys, or of none: nothing to resolve, so nothing to ask again (design D27) */
		private Map<AttributeReferenceKey, BagOfValues> resolved(List<AttributeReferenceKey> keys){
			if(keys.isEmpty()){
				return ImmutableMap.of();
			}
			ImmutableMap.Builder<AttributeReferenceKey, BagOfValues> contextKeys = ImmutableMap.builderWithExpectedSize(keys.size());
			for(int i = 0; i < keys.size(); i++){
				AttributeReferenceKey key = keys.get(i);
				Optional<BagOfValues> value = context.resolve(key);
				if(value.isPresent()){
					contextKeys.put(key, value.get());
				}
			}
			return contextKeys.build();
		}

		public boolean isCacheable(){
			return resolver
					.isCacheable();
		}


		public Function<ResolverContext, Optional<?>> getResolverFunction() {
			return resolver.getResolverFunction();
		}

		@Override
		public String toString(){
			return MoreObjects.toStringHelper(this)
					.add("context", context)
					.add("resolver", resolver)
					.add("resolvedKeys", resolvedKeys)
					.toString();
		}
	}

}
