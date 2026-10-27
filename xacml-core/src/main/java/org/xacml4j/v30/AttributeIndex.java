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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
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
		List<Attribute> found = attributes(attributeId);
		return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
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
	public Collection<Attribute> findByIssuer(final String attributeId, final String issuer){
		// A new modifiable list, as the stream code returned (public behavior, change
		// optimize-pdp-evaluation D6 characterization)
		List<Attribute> candidates = attributes(attributeId);
		if(issuer == null){
			return new ArrayList<>(candidates);
		}
		List<Attribute> found = new ArrayList<>(candidates.size());
		for(int i = 0; i < candidates.size(); i++){
			Attribute a = candidates.get(i);
			if(hasIssuer(a, issuer)){
				found.add(a);
			}
		}
		return found;
	}

	public Collection<Attribute> find(Predicate<Attribute> attributePredicate){
		Objects.requireNonNull(attributePredicate);
		List<Attribute> found = new ArrayList<>(0);
		for(Attribute a : attributes.values()){
			if(attributePredicate.test(a)){
				found.add(a);
			}
		}
		return found;
	}

	public Optional<Attribute> findFirst(Predicate<Attribute> attributePredicate){
		Objects.requireNonNull(attributePredicate);
		for(Attribute a : attributes.values()){
			if(attributePredicate.test(a)){
				return Optional.of(a);
			}
		}
		return Optional.empty();
	}

	public Collection<Attribute> find(String id, Predicate<Attribute> attributePredicate){
		Objects.requireNonNull(attributePredicate);
		List<Attribute> candidates = attributes(id);
		List<Attribute> found = new ArrayList<>(candidates.size());
		for(int i = 0; i < candidates.size(); i++){
			Attribute a = candidates.get(i);
			if(attributePredicate.test(a)){
				found.add(a);
			}
		}
		return found;
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
	public <T extends Value<?>> Collection<T> findValues(
			String attributeId,
			ValueType dataType){
		return findValues(attributeId, dataType, null);
	}

	/**
	 * Gets all {@link Value} instances
	 * contained in this attributes defaultProvider
	 *
	 * @param attributeId an attribute id
	 * @param type an attribute value data type
	 * @param issuer an attribute issuer
	 * @return a collection of {@link Value} instances
	 */
	@SuppressWarnings("unchecked")
	public <T extends Value<?>> Collection<T> findValues(
			String attributeId, final ValueType type, String issuer){
		Preconditions.checkNotNull(type);
		// Loops, not streams: this runs for every attribute lookup of a decision (change
		// optimize-pdp-evaluation, D6). Same values in the same order as before.
		List<Attribute> candidates = attributes(attributeId);
		ImmutableList.Builder<T> b = null;
		List<Value<?>> only = null;
		for(int i = 0; i < candidates.size(); i++){
			Attribute a = candidates.get(i);
			if(!hasIssuer(a, issuer)){
				continue;
			}
			List<Value<?>> values = valueList(a);
			int matching = countOfType(values, type);
			if(matching == 0){
				continue;
			}
			if(b == null && only == null && matching == values.size()){
				// So far one attribute, all of whose values have the type: its own immutable list.
				only = values;
				continue;
			}
			if(b == null){
				b = ImmutableList.builder();
				if(only != null){
					b.addAll((List<T>)only);
					only = null;
				}
			}
			for(int j = 0; j < values.size(); j++){
				Value<?> v = values.get(j);
				if(isOfType(v, type)){
					b.add((T)v);
				}
			}
		}
		if(b != null){
			return b.build();
		}
		return only != null ? (Collection<T>)only : ImmutableList.of();
	}

	public boolean hasValues(
			final String attributeId, final ValueType type, final String issuer){
		Preconditions.checkNotNull(type);
		List<Attribute> candidates = attributes(attributeId);
		for(int i = 0; i < candidates.size(); i++){
			Attribute a = candidates.get(i);
			if(hasIssuer(a, issuer) && countOfType(valueList(a), type) > 0){
				return true;
			}
		}
		return false;
	}

	/** @return the attributes with the id, as stored (an immutable list of the multimap) */
	private List<Attribute> attributes(String attributeId){
		Collection<Attribute> found = attributes.get(attributeId);
		return found instanceof List ? (List<Attribute>)found : ImmutableList.copyOf(found);
	}

	/**
	 * @return the values of the attribute as stored (an immutable list); no accessor is added to
	 * {@link Attribute}, whose default serialVersionUID counts its non-private methods (review RVC1)
	 */
	@SuppressWarnings("unchecked")
	private static List<Value<?>> valueList(Attribute a){
		Collection<Value<?>> values = a.values();
		return values instanceof List ? (List<Value<?>>)values : ImmutableList.copyOf(values);
	}

	private static boolean hasIssuer(Attribute a, String issuer){
		return issuer == null || Objects.equals(issuer, a.getIssuer());
	}

	/** The type test of {@link Attribute#getValuesByType}: the value's type equals the type. */
	private static boolean isOfType(Value<?> v, ValueType type){
		return Objects.equals(v.getEvaluatesTo(), type);
	}

	private static int countOfType(List<Value<?>> values, ValueType type){
		int n = 0;
		for(int i = 0; i < values.size(); i++){
			if(isOfType(values.get(i), type)){
				n++;
			}
		}
		return n;
	}
	public <T extends Value<?>> Optional<T> findValue(
			String attributeId, final ValueType type, String issuer){
		return findValue(attributeId, type, issuer, a->true);
	}

	public <T extends Value<?>> Optional<T> findValue(
			String attributeId, final ValueType type, String issuer, Predicate<T>  filter){
		Preconditions.checkNotNull(type);
		List<Attribute> candidates = attributes(attributeId);
		for(int i = 0; i < candidates.size(); i++){
			Attribute a = candidates.get(i);
			if(!hasIssuer(a, issuer)){
				continue;
			}
			// The stream filter rejected a null filter for every attribute it reached
			Objects.requireNonNull(filter);
			List<Value<?>> values = valueList(a);
			for(int j = 0; j < values.size(); j++){
				@SuppressWarnings("unchecked")
				T v = (T)values.get(j);
				if(isOfType(v, type) && filter.test(v)){
					return Optional.of(v);
				}
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
