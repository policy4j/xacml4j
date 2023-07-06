package org.xacml4j.v30;

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
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for XACML attribute containers
 *
 * @author Giedrius Trumpickas
 */
public final class AttributeIndex
{
	private final Multimap<String, Attribute> attributes;

	private transient int hashCode;

	private final static AttributeIndex EMPTY = AttributeIndex.builder().build();

	private AttributeIndex(Builder b){
		this.attributes = b.attrsBuilder.build();
	}

	public static Builder builder(){
		return new Builder();
	}

	public static AttributeIndex empty(){
		return EMPTY;
	}

	/**
	 * Gets all attributes with a given identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return a collection of attributes if there is no
	 * attributes with a given identifier empty collection
	 * is returned
	 */
	public Collection<Attribute> get(String attributeId){
		return attributes.get(attributeId);
	}

	/**
	 * Gets a single {@link Attribute} defaultProvider with
	 * a given attribute identifier
	 *
	 * @param attributeId an attribute identifier
	 * @return {@link Attribute} defaultProvider or {@code null}
	 * if no attribute available with a given identifier
	 */
	public Optional<Attribute> single(String attributeId){
		return attributes.get(attributeId)
				.stream()
				.findFirst();
	}

	public boolean isEmpty(){
		return attributes.isEmpty();
	}

	/**
	 * Gets all attributes with a given attribute
	 * identifier and issuer
	 *
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @return a collection of attributes with a given identifier
	 * and given issuer
	 */
	public Collection<Attribute> find(final String attributeId, final String issuer){
		return attributes.get(attributeId)
				          .stream()
				         .filter(a -> issuer!= null?
								 Objects.equals(issuer, a.getIssuer()):true).collect(Collectors.toList());
	}

	public Collection<Attribute> find(Predicate<Attribute> attributePredicate){
		return stream().filter(attributePredicate)
				.collect(Collectors.toList());
	}

	public Optional<Attribute> findFirst(Predicate<Attribute> attributePredicate){
		return attributes.values().stream()
				.filter(attributePredicate)
				.findFirst();
	}

	public Collection<Attribute> find(String id, Predicate<Attribute> attributePredicate){
		return attributes.get(id).stream()
				.filter(attributePredicate)
				.collect(Collectors.toList());
	}

	public Stream<Attribute> stream(){
		return attributes.values()
				.stream();
	}

	/**
	 * Gets all attribute {@link Attribute}
	 * instances
	 *
	 * @return immutable collection of {@link Attribute}
	 * instances
	 */
	public Collection<Attribute> attributes() {
		return attributes.values();
	}


	/**
	 * @see AttributeIndex#findValues(String, String, ValueType)
	 */
	public <T extends Value<T>> Collection<T> findValues(
			String attributeId,
			ValueType dataType){
		return findValues(attributeId, null, dataType);
	}

	/**
	 * Gets all {@link Value} instances
	 * contained in this attributes defaultProvider
	 *
	 * @param attributeId an attribute id
	 * @param issuer an attribute issuer
	 * @param type an attribute value data type
	 * @return a collection of {@link Value} instances
	 */
	public <T extends Value<T>> Collection<T> findValues(
			String attributeId, String issuer, final ValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = find(attributeId, issuer);
		ImmutableList.Builder<T> b = ImmutableList.builder();
		for(Attribute a : found){
			b.addAll(a.getValuesByType(type));
		}
		return b.build();
	}

	public boolean hasValues(
			String attributeId, String issuer, final ValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = find(attributeId, issuer);
		if(found.isEmpty()){
			return false;
		}
		for(Attribute a : found){
			if(!a.getValuesByType(type).isEmpty()){
				return true;
			}
		}
		return false;
	}

	public <T extends Value<T>> Optional<T> findValue(
			String attributeId, final ValueType type, String issuer, Predicate<T>  filter){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = find(attributeId, issuer);
		for(Attribute a : found){
			Collection<T> values = a.getValuesByType(type);
			Optional<T> v = values.stream()
					.filter(filter)
					.findFirst();
			if(v.isPresent()){
				return v;
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof AttributeIndex)) {
			return false;
		}
		AttributeIndex a = (AttributeIndex) o;
		return attributes.values().containsAll(a.attributes.values()) &&
				a.attributes.values().containsAll(attributes.values());
	}

	@Override
	public int hashCode(){
		if(hashCode == 0){
			hashCode = attributes.hashCode();;
		}
		return hashCode;
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("attr", attributes.toString())
				.toString();
	}


	public final static class Builder
	{
		private ImmutableListMultimap.Builder<String, Attribute> attrsBuilder = ImmutableListMultimap.builder();

		public Builder attribute(Attribute... attrs) {
			Objects.requireNonNull(attrs,
					"At least one attribute must be specified");
			for(Attribute a : attrs){
				Objects.requireNonNull(a, "Attribute");
				attrsBuilder.put(a.getAttributeId(), a);
			}
			return this;
		}

		public Builder noAttributes() {
			this.attrsBuilder = ImmutableListMultimap.builder();
			return this;
		}

		public Builder attributes(Iterable<Attribute> attrs) {
			if(attrs == null){
				return this;
			}
			for(Attribute attr : attrs){
				attrsBuilder.put(attr.getAttributeId(), attr);
			}
			return this;
		}

		public AttributeIndex build(){
			return new AttributeIndex(this);
		}

	}
}
