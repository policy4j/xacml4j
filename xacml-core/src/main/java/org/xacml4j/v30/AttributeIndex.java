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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base class for XACML attribute containers
 *
 * @author Giedrius Trumpickas
 */
public final class AttributeIndex
{
	private static final VarHandle BAGS;

	static {
		try{
			BAGS = MethodHandles.lookup().findVarHandle(AttributeIndex.class, "bags", ConcurrentMap.class);
		}catch(ReflectiveOperationException e){
			throw new ExceptionInInitializerError(e);
		}
	}

	private final Multimap<String, Attribute> attributes;
	/** Whether an attribute is marked to be included in the result; known since the index was built (design D32) */
	private final boolean includeInResult;
	/** The attributes marked to be included in the result, in the order of {@link #attributes()} (cycle AM) */
	private final ImmutableList<Attribute> includedInResult;
	/**
	 * The bags resolved against this index, by lookup and by designator, each built on the first
	 * demand and kept, so that a reused index costs no bag per decision; created on the first bag
	 * (change optimize-pdp-evaluation, cycle AF). Only a key with values is kept, so the map is
	 * bounded by the stored attributes: an attribute id, a type of its values, and no issuer or
	 * its issuer (review R-FABLE2-sol), and by the designators that found them (cycle AM).
	 */
	private transient volatile ConcurrentMap<Object, Optional<BagOfValues>> bags;

	private transient int hashCode;

	private final static AttributeIndex EMPTY = AttributeIndex.builder().build();

	private AttributeIndex(Builder b){
		this.attributes = b.attrsBuilder.build();
		this.includeInResult = b.includeInResult;
		this.includedInResult = includedInResult(b);
	}

	/**
	 * The marked attributes in the order the builder added them, which is the order of the index
	 * unless an attribute id repeats: the index groups the attributes of an id (cycle AM)
	 */
	private ImmutableList<Attribute> includedInResult(Builder b){
		if(!includeInResult){
			return ImmutableList.of();
		}
		if(attributes.size() == attributes.asMap().size()){
			return b.included.build();
		}
		ImmutableList.Builder<Attribute> grouped = ImmutableList.builder();
		for(Attribute a : attributes.values()){
			if(a.isIncludeInResult()){
				grouped.add(a);
			}
		}
		return grouped.build();
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

	/** @return whether an attribute of this index is marked to be included in the result (design D32) */
	public boolean hasIncludeInResultAttributes(){
		return includeInResult;
	}

	/**
	 * @return the attributes marked to be included in the result, in the order of
	 * {@link #attributes()}; known since the index was built (cycle AM)
	 */
	public Collection<Attribute> includeInResultAttributes(){
		return includedInResult;
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

	/**
	 * The bag of the values of {@link #findValues(String, ValueType, String)}, built on the first
	 * demand of the attribute id, type, and issuer and kept for the next ones (cycle AF).
	 * Absence is answered from the attributes each time, as it costs no bag.
	 *
	 * @param attributeId an attribute id
	 * @param type an attribute value data type
	 * @param issuer an attribute issuer, or {@code null} for any
	 * @return the bag of the values, if there are any
	 */
	public Optional<BagOfValues> resolve(String attributeId, ValueType type, String issuer){
		Preconditions.checkNotNull(type);
		if(!attributes.containsKey(attributeId)){
			// Absent: no lookup to build (cycle AM)
			return Optional.empty();
		}
		Lookup lookup = new Lookup(attributeId, type, issuer);
		ConcurrentMap<Object, Optional<BagOfValues>> kept = bags;
		Optional<BagOfValues> bag = kept != null? kept.get(lookup): null;
		if(bag != null){
			return bag;
		}
		Collection<Value<?>> values = findValues(attributeId, type, issuer);
		if(values.isEmpty()){
			return Optional.empty();
		}
		bag = Optional.of(type.bagType().builder().attributes(values).build());
		Optional<BagOfValues> first = bags().putIfAbsent(lookup, bag);
		return first != null? first: bag;
	}

	/**
	 * The bag of {@link #resolve(String, ValueType, String)} for the attribute, type, and issuer
	 * of the designator. The category of the designator plays no part: the index belongs to
	 * whatever category holds it, as with
	 * {@link org.xacml4j.v30.types.Entity#resolve(AttributeSelectorKey)}. The bag is kept under the
	 * designator too, so a later demand by the designator builds no lookup; every demand sees the
	 * one {@code Optional} that the lookup kept (design D17, cycle AM).
	 *
	 * @param key an attribute designator
	 * @return the bag of the designator's values, if there are any
	 */
	public Optional<BagOfValues> resolve(AttributeDesignatorKey key){
		ConcurrentMap<Object, Optional<BagOfValues>> kept = bags;
		Optional<BagOfValues> bag = kept != null? kept.get(key): null;
		if(bag != null){
			return bag;
		}
		bag = resolve(key.getAttributeId(), key.getDataType(), key.getIssuer());
		if(bag.isEmpty()){
			return bag;
		}
		Optional<BagOfValues> first = bags().putIfAbsent(key, bag);
		return first != null? first: bag;
	}

	/** The kept bags, created on the first one; concurrent first demands keep one map */
	@SuppressWarnings("unchecked")
	private ConcurrentMap<Object, Optional<BagOfValues>> bags(){
		ConcurrentMap<Object, Optional<BagOfValues>> kept = bags;
		if(kept == null){
			kept = new ConcurrentHashMap<>(attributes.keySet().size());
			ConcurrentMap<Object, Optional<BagOfValues>> raced =
					(ConcurrentMap<Object, Optional<BagOfValues>>) BAGS.compareAndExchange(this, null, kept);
			if(raced != null){
				kept = raced;
			}
		}
		return kept;
	}

	/** A lookup of values by attribute id, type, and issuer ({@code null} for any): the key of a kept bag */
	private record Lookup(String attributeId, ValueType type, String issuer) {}

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
		private boolean includeInResult;
		/** The attributes marked to be included in the result, created by the first one (cycle AM) */
		private ImmutableList.Builder<Attribute> included;

		public Builder attribute(Attribute... attrs) {
			Objects.requireNonNull(attrs,
					"At least one attribute must be specified");
			for(Attribute a : attrs){
				Objects.requireNonNull(a, "Attribute");
				add(a);
			}
			return this;
		}

		public Builder noAttributes() {
			this.attrsBuilder = ImmutableListMultimap.builder();
			this.includeInResult = false;
			this.included = null;
			return this;
		}

		public Builder attributes(Iterable<Attribute> attrs) {
			if(attrs == null){
				return this;
			}
			for(Attribute attr : attrs){
				add(attr);
			}
			return this;
		}

		private void add(Attribute a){
			attrsBuilder.put(a.getAttributeId(), a);
			if(a.isIncludeInResult()){
				includeInResult = true;
				if(included == null){
					included = ImmutableList.builder();
				}
				included.add(a);
			}
		}

		public AttributeIndex build(){
			return new AttributeIndex(this);
		}

	}
}
