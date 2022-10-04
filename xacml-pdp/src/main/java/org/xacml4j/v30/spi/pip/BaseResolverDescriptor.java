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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

abstract class BaseResolverDescriptor<R>
	implements ResolverDescriptor<R>
{
	private String id;
	private String name;

	private CategoryId categoryId;
	private List<AttributeReferenceKey> allContextKeys;
	private Duration cacheTTL;

	protected BaseResolverDescriptor(
			BaseBuilder builder) {
		this.id = Objects.requireNonNull(builder.id, "id");
		this.name = Objects.requireNonNull(builder.name, "name");
		this.categoryId = Objects.requireNonNull(builder.categoryId, "category");
		this.cacheTTL = builder.cacheTTL;
		this.allContextKeys = ImmutableList.copyOf(builder.contextReferenceKeys);
	}

	@Override
	public final String getId(){
		return id;
	}

	@Override
	public final String getName(){
		return name;
	}


	@Override
	public boolean isCacheable() {
		return !(cacheTTL.isNegative() || cacheTTL.isZero());
	}

	@Override
	public Duration getPreferredCacheTTL() {
		return cacheTTL;
	}

	public final CategoryId getCategory() {
		return categoryId;
	}

	@Override
	public Map<AttributeReferenceKey, BagOfValues> resolveKeyRefs(
			ResolverContext context) {
		ImmutableMap.Builder<AttributeReferenceKey, BagOfValues> contextKeys = ImmutableMap.builder();
		allContextKeys.forEach((k)->context.resolve(k)
		                                   .ifPresent((v)->contextKeys.put(k, v)));
		return contextKeys.build();
	}

	public List<AttributeReferenceKey> getAllContextKeyRefs(){
		return allContextKeys;
	}
}
