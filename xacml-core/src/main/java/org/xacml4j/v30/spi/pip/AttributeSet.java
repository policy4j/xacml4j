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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.xacml4j.util.Pair;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeValues;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class AttributeSet
{
	private final Instant timestamp;
	private final Map<String, BagOfAttributeValues> values;
	private final AttributeResolverDescriptor d;
	private transient int hashCode;

	private AttributeSet(Builder b){
		this.d = b.d;
		this.values = b.mapBuilder.build();
		this.timestamp = b.ticker.instant();
	}

	public static Builder builder(AttributeResolverDescriptor d){
		return new Builder(d);
	}

	/**
	 * Gets attribute resolver descriptor
	 *
	 * @return {@link AttributeResolverDescriptor}
	 */
	public AttributeResolverDescriptor getDescriptor(){
		return d;
	}

	public Stream<Pair<String, BagOfAttributeValues>> stream(){
		return values.entrySet()
				.stream()
				.map(v->Pair.of(v.getKey(), v.getValue()));
	}

	/**
	 * Gets a time when this attribute set was created
	 *
	 * @return a time stamp in nanoseconds
	 */
	public Instant getCreatedTime(){
		return timestamp;
	}

	/**
	 * Gets issuer for attributes in this set
	 *
	 * @return an issuer for attributes in this set
	 */
	public String getIssuer(){
		return d.getIssuer();
	}

	public Iterable<AttributeDesignatorKey> getAttributeKeys(){
		return d.getAttributesByKey().keySet();
	}

	/**
	 * Gets an attribute value for a given designator
	 *
	 * @param key an attribute designator
	 * @return {@link BagOfAttributeValues}
	 */
	public Optional<BagOfAttributeValues> get(AttributeDesignatorKey key)
	{
		AttributeDescriptor ad = d.getAttributesByKey().get(key);
		return Optional.ofNullable(values.get(ad.getAttributeId()));
	}

	/**
	 * Gets attribute values from this set by the
	 * attribute identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link BagOfAttributeValues}
	 */
	public Optional<BagOfAttributeValues> get(String attributeId)
	{
		return d.getAttribute(attributeId)
				.map(v->values.get(attributeId));

	}

	public Iterable<String> getAttributeIds(){
		return d.getProvidedAttributeIds();
	}

	public boolean isEmpty() {
		for (BagOfAttributeValues v : values.values()) {
			if (!v.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public Instant getTimestamp(){
		return timestamp;
	}

	public int size(){
		return d.getAttributesCount();
	}


	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("id", d.getId())
		.add("issuer", d.getIssuer())
		.add("values", values)
		.toString();
	}

	@Override
	public int hashCode(){
		if(hashCode == 0){
			this.hashCode = Objects.hash(d, values);
		}
		return hashCode;
	}

	public static class Builder
	{
		private Clock ticker = Clock.systemUTC();
		private AttributeResolverDescriptor d;
		private ImmutableMap.Builder<String, BagOfAttributeValues> mapBuilder = ImmutableMap.builder();

		Builder(AttributeResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
		}

		public Builder resolver(AttributeResolverDescriptor d){
			Preconditions.checkNotNull(d);
			this.d = d;
			return this;
		}

		public Builder clock(Clock t){
			this.ticker = t;
			return this;
		}

		public Builder attribute(String id, BagOfAttributeValues value){
			Preconditions.checkNotNull(id);
			Preconditions.checkNotNull(value);
			AttributeDescriptor attrDesc = d.getAttribute(id)
					.orElseThrow(()-> new IllegalArgumentException(String.format(
							"Attribute=\"%s\" is not defined by resolver=\"%s\"",
							id, d.getId())));
			if(attrDesc.getDataType().equals(value.getDataType())){
				throw new IllegalArgumentException(String.format(
						"Given attribute=\"%s\" value has wrong type=\"%s\", type=\"%s\" is expected",
						id, value.getDataType(), attrDesc.getDataType()));
			}
			this.mapBuilder.put(id, value);
			return this;
		}

		public Builder attributes(Map<String, BagOfAttributeValues> attributes){
			if(attributes == null){
				return this;
			}
			for(Entry<String, BagOfAttributeValues> e : attributes.entrySet()){
				attribute(e.getKey(), e.getValue());
			}
			return this;
		}

		public AttributeSet build(){
			return new AttributeSet(this);
		}
	}
}
