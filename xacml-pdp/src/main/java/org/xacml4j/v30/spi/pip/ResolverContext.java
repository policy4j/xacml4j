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
import java.util.HashMap;
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

	<V> Function<ResolverContext, V> getResolver();

	/**
	 * Resolves context key
	 */
	Optional<BagOfValues> resolve(AttributeReferenceKey key);


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
		private Optional<Content.Type> contentType;
		private EvaluationContext context;
		private Map<AttributeReferenceKey, BagOfValues> resolvedKeys;
		private ResolverDescriptor resolver;

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

		@Override
		public ResolverDescriptor getDescriptor(){
			return resolver;
		}

		public Map<AttributeReferenceKey, BagOfValues> getResolvedKeys(){
			if(resolvedKeys == null){
				this.resolvedKeys = resolver.resolveKeyRefs(this);
			}
			return resolvedKeys;
		}

		@Override
		public Optional<BagOfValues> resolve(AttributeReferenceKey key)
		{
			BagOfValues cachedValue = resolvedKeys != null ? resolvedKeys.get(key) : null;
			if(cachedValue != null){
				return Optional.ofNullable(cachedValue);
			}
			Optional<BagOfValues> resolvedValue = context.resolve(key);
			resolvedValue.ifPresent((v)->{
				if(resolvedKeys == null){
					this.resolvedKeys = new HashMap<>();
				}
				resolvedKeys.putIfAbsent(key, v);
			});
			return resolvedValue;
		}

		public Supplier<Optional<BagOfValues>> resolveRef(AttributeReferenceKey referenceKey){
			Optional<BagOfValues> v = resolve(referenceKey);
			v.ifPresent(bagOfAttributeValues -> resolvedKeys.put(referenceKey, bagOfAttributeValues));
			return ()->v;
		}

		public boolean isCacheable(){
			return resolver
					.isCacheable();
		}


		public Function<ResolverContext, Optional<?>> getResolver() {
			return resolver.getResolver();
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
