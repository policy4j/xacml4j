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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.xacml4j.v30.BagOfValues;

import com.google.common.base.MoreObjects;
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

	private static final BagOfValues[] NO_KEYS = new BagOfValues[0];

	private final String resolverId;
	private final BagOfValues[] keys;

	private int hashCode = -1;

	public ResolverCacheKey(Builder b) {
		this(b.id, b.keys.toArray(NO_KEYS));
	}

	private ResolverCacheKey(String resolverId, BagOfValues[] keys) {
		this.resolverId = resolverId;
		this.keys = keys;
	}

	public static Builder builder(String resolverId){
		return new Builder().id(resolverId);
	}

	/**
	 * The key of a resolver call from the resolved values of its context keys, in their order,
	 * copied once into the key without a builder (change optimize-pdp-evaluation, cycle AL). It
	 * equals the key the builder makes of the same values.
	 */
	static ResolverCacheKey of(String resolverId, Collection<BagOfValues> keys){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(resolverId));
		return new ResolverCacheKey(resolverId, keys.toArray(NO_KEYS));
	}

	@Override
	public int hashCode(){
		int h = hashCode;
		if(h == -1){
			// By the contents of the bags, as equals compares them (bead policy4j-3huw, design D27)
			h = 31 * resolverId.hashCode() + Arrays.deepHashCode(keys);
			hashCode = h;
		}
		return h;
	}

	/**
	 * A stream written before the hash was computed by content may carry the cached hash of
	 * that time; it is dropped, so the key is computed again (design D27)
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		hashCode = -1;
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
				Arrays.deepEquals(keys, k.keys);
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
		private final List<BagOfValues> keys = new ArrayList<>(2);

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

		public Builder key(BagOfValues...keys){
			keys(Arrays.asList(keys));
			return this;
		}

		public Builder keys(Iterable<BagOfValues> keys){
			for(BagOfValues bag : keys){
				this.keys.add(bag);
			}
			return this;
		}

		public ResolverCacheKey build(){
			return new ResolverCacheKey(this);
		}

	}
}
