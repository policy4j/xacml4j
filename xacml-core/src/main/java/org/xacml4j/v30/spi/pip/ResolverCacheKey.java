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

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.MoreObjects;
import org.xacml4j.v30.BagOfAttributeValues;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * A key used to identify uniquely resolver
 * resolution result
 *
 * @author Giedrius Trumpickas
 */
public final class ResolverCacheKey implements Serializable
{
	private static final long serialVersionUID = -6895205924708410228L;

	private final String resolverId;
	private final BagOfAttributeValues[] keys;

	public ResolverCacheKey(Builder b) {
		this.resolverId = b.id;
		this.keys = b.keysBuilder.toArray(
				new BagOfAttributeValues[b.keysBuilder.size()]);
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public int hashCode(){
		return java.util.Objects.hash(resolverId, keys);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this) {
			return true;
		}
		if (!(o instanceof ResolverCacheKey)) {
			return false;
		}
		ResolverCacheKey k = (ResolverCacheKey)o;
		return resolverId.equals(k.resolverId) &&
				Arrays.equals(keys, k.keys);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("id", resolverId)
		.add("keys", Arrays.toString(keys))
		.toString();
	}

	public static class Builder
	{
		private String id;
		private List<BagOfAttributeValues> keysBuilder = new LinkedList<>();

		public Builder id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.id = id;
			return this;
		}

		public Builder id(Resolver<?> r){
			return id(r.getDescriptor().getId());
		}

		public Builder id(ResolverDescriptor d){
			return id(d.getId());
		}

		public Builder key(BagOfAttributeValues...keys){
			keys(Arrays.asList(keys));
			return this;
		}

		public Builder keys(Iterable<BagOfAttributeValues> keys){
			for(BagOfAttributeValues bag : keys){
				keysBuilder.add(bag);
			}
			return this;
		}

		public ResolverCacheKey build(){
			return new ResolverCacheKey(this);
		}

	}
}
