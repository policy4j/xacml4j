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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.CategoryId;

import java.util.List;

public abstract class BaseResolverDescriptor
	implements ResolverDescriptor
{
	private String id;
	private String name;

	private CategoryId category;
	private List<AttributeReferenceKey> keyRefs;
	private int cacheTTL;

	protected BaseResolverDescriptor(String id,
			String name,
			CategoryId category,
			List<AttributeReferenceKey> keys){
		this(id, name, category, keys, 0);
	}

	protected BaseResolverDescriptor(
			String id,
			String name,
			CategoryId category,
			List<AttributeReferenceKey> keys,
			int preferredCacheTTL) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(category);
		this.id = id;
		this.name = name;
		this.category = category;
		this.keyRefs = ImmutableList.copyOf(keys);
		this.cacheTTL = (preferredCacheTTL < 0)?0:preferredCacheTTL;
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

	@Override
	public final CategoryId getCategory() {
		return category;
	}

	@Override
	public List<AttributeReferenceKey> getKeyRefs() {
		return keyRefs;
	}
}
