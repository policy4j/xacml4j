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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;

/**
 * XACML resolver descriptor
 *
 * @param <R>
 * @author Giedrius Trumpickas
 */
public interface ResolverDescriptor<R>
{
	/**
	 * An unique identifier for
	 * this attribute resolver
	 *
	 * @return an unique identifier
	 */
	String getId();

	/**
	 * Gets resolver name
	 *
	 * @return resolver name
	 */
	String getName();

	/**
	 * Gets resolver function
	 *
	 * @return resolver function
	 */
	Function<ResolverContext, Optional<R>> getResolver();


	/**
	 * Tests if this attribute reference key can be resolved
	 *
	 * @param ref an attribute reference
	 * @return true if it can be resolved
	 */
	boolean canResolve(AttributeReferenceKey ref);

	/**
	 * Resolved key references from the context
	 *
	 * @param context a context
	 * @return a map with resolved key references
	 */
	Map<AttributeReferenceKey, BagOfValues> resolveKeyRefs(ResolverContext context);

	List<AttributeReferenceKey> getAllContextKeyRefs();

	/**
	 * Gets supported categories
	 *
	 * @return collection of supported categories
	 */
	CategoryId getCategory();
	
	/**
	 * Test if values resolved by resolver can be cached by PIP
	 *
	 * @return {@code true} if value can be cached
	 */
	boolean isCacheable();

	/**
	 * Gets preferred cache TTL for an attributes resolved via this resolver
	 *
	 * @return a TTL in seconds or {@code 0}
	 */
	Duration getPreferredCacheTTL();

	abstract class BaseBuilder<R, T extends BaseBuilder<R, ?>>
	{
		protected String id;
		protected String name;
		protected CategoryId categoryId;
		protected Duration cacheTTL;
		protected List<Function<ResolverContext, Optional<BagOfValues>>> contextKeysResolutionPlan;
		protected List<AttributeReferenceKey> contextReferenceKeys;


		protected BaseBuilder(String id, String name,
							  CategoryId categoryId)
		{
			this.id = Objects.requireNonNull(id, "id");
			this.name = Objects.requireNonNull(name, "name");
			this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
			this.contextKeysResolutionPlan = new LinkedList<>();
			this.contextReferenceKeys = new LinkedList<>();
		}

		public T cache(int ttl){
			this.cacheTTL = Duration.ofSeconds(ttl);
			return getThis();
		}

		protected T contextRefOrElse(
				AttributeReferenceKey a,
						   AttributeReferenceKey b){
			this.contextReferenceKeys.add(a);
			this.contextReferenceKeys.add(b);
			return orElse((context -> context.resolve(a)),
					(context -> context.resolve(b)));
		}

		public T contextRef(AttributeReferenceKey a){
			this.contextReferenceKeys.add(a);
			this.contextKeysResolutionPlan.add((context -> context.resolve(a)));
			return getThis();
		}

		protected T orElse(Function<ResolverContext, Optional<BagOfValues>> a,
						   Function<ResolverContext, Optional<BagOfValues>> b){
			this.contextKeysResolutionPlan.add(
					(context)-> a.apply(context).or(()->b.apply(context)));
			return getThis();
		}

		public T noCache(){
			this.cacheTTL = Duration.ZERO;
			return getThis();
		}

		protected abstract T getThis();
	}
}
