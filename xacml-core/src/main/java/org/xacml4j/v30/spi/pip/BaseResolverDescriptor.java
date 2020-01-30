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

import java.util.*;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.CategoryId;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class BaseResolverDescriptor
	implements ResolverDescriptor
{
	private String id;
	private String name;

	private Collection<CategoryId> categoryIds;
	private List<Function<ResolverContext, Optional<BagOfAttributeValues>>> contextKeysResolutionPlan;
	private int cacheTTL;

	protected BaseResolverDescriptor(
			String id,
			String name,
			Collection<CategoryId> categoryIds,
			int preferredCacheTTL,
			Collection<Function<ResolverContext, Optional<BagOfAttributeValues>>> contextKeysResolutionPlan) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.categoryIds = Objects.requireNonNull(Collections.unmodifiableSet(new HashSet<>(categoryIds)), "categoryIds");
		this.cacheTTL = (preferredCacheTTL < 0)?0:preferredCacheTTL;
		this.contextKeysResolutionPlan = Collections.unmodifiableList(new LinkedList<>(contextKeysResolutionPlan));
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
		return cacheTTL > 0;
	}

	@Override
	public int getPreferredCacheTTL() {
		return cacheTTL;
	}

	public final Collection<CategoryId> getCategoryIds() {
		return categoryIds;
	}


	@Override
	public Map<AttributeReferenceKey, BagOfAttributeValues> resolveKeyRefs(
			ResolverContext context) {
		contextKeysResolutionPlan.forEach(f->f.apply(context));
		return context.getResolvedKeys();
	}
}
