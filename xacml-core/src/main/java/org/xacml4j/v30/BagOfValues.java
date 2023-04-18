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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * XACML defines implicit collections of its data-types.
 * XACML refers to a collection of values that are of a single data-type as a bag.
 * Bags of data-types are needed because selections of nodes from an XML resource
 * or XACML request context may return more than one value.
 *
 * The values in a bag are not ordered, and some of the values may be duplicates.
 * There SHALL be no notion of a bag containing bags, or a bag containing values
 * of differing types. I.e. a bag in XACML SHALL contain only values that
 * are of the same data-type.
 *
 * @author Giedrius Trumpickas
 */
public final class BagOfValues
		implements ValueExp, Serializable, Iterable<Value>
{
	private static final long serialVersionUID = -8197446176793438616L;

	private final BagOfValuesType type;
	private final Multiset<Value> values;
	private int hashCode;
	
	/**
	 * Constructs bag of attributes.
	 *
	 * @param type a bag attribute type
	 * @param attributes a collection of attributes
	 */
	private BagOfValues(Builder b){
		this.type = b.bagType;
		this.values = b.valuesBuilder.build();
	}

	@Override
	public Iterator<Value> iterator() {
		return values.iterator();
	}

	@Override
	public void forEach(Consumer<? super Value> action) {
		values.forEach(action);
	}

	@Override
	public Spliterator<Value> spliterator() {
		return values.spliterator();
	}

	@SuppressWarnings("unchecked")
	public <T extends Value> Collection<T> values() {
		return (Collection<T>)values;
	}

	public Stream<Value> stream(){
		return values.stream();
	}

	/**
	 * Gets bag value data type
	 *
	 * @return {@link ValueType}
	 */
	public ValueType getBagValueType(){
		return type.getValueType();
	}

	@Override
	public BagOfValuesType getEvaluatesTo() {
		return type;
	}

	/**
	 * Gets first attribute value
	 * in this bag
	 *
	 * @return first {@link Value}
	 * defaultProvider in this bag
	 * @exception NoSuchElementException if bag is empty
	 */
	public <T extends Value> T value(){
		return (T)values.iterator().next();
	}

	public <T extends Value> Optional<T> single() {
		return Optional.ofNullable(Iterables.getFirst(this.<T>values(), null));
	}

	/**
	 * Gets number of {@link Value}
	 * instances in this bag
	 *
	 * @return number of attributes
	 * in this bag
	 */
	public int size() {
		return values.size();
	}

	/**
	 * Tests if this bag is empty.
	 *
	 * @return {@code true} if bag is empty
	 */
	public boolean isEmpty(){
		return values.isEmpty();
	}

	/**
	 * Tests if this bag contains given attribute.
	 *
	 * @param attr an attribute
	 * @return {@code true} if bag contains given attribute
	 */
	public boolean contains(Value attr){
		return values.contains(attr);
	}

	/**
	 * Returns a bag of such that it contains all elements of
	 * all the argument bags all duplicate elements are removed
	 *
	 * @param bag an another bag
	 * @return union of this and given bag without duplicate
	 * elements
	 */
	public BagOfValues union(BagOfValues bag)
	{
		Preconditions.checkArgument(type.equals(bag.type));
		return type.builder()
		           .attributes(Sets.union(values.elementSet(),
		                                  bag.values.elementSet()))
		           .build();
	}

	/**
	 * Returns a bag of values such that it contains only
	 * elements that are common between this and given bag
	 *
	 * @param bag an another bag
	 * @return bag which contains common
	 * elements between this and given bag
	 */
	public BagOfValues intersection(BagOfValues bag)
	{
		Preconditions.checkArgument(type.equals(bag.type));
		return type.builder()
		           .attributes(Sets.intersection(values.elementSet(),
		                                         bag.values.elementSet()))
		           .build();
	}

	/**
	 * Tests if this bag contains at least on value
	 * from the given bag
	 *
	 * @param bag a bag
	 * @return {@code true} if this bag contains
	 * at least one value from the given bag
	 */
	public boolean containsAtLeastOneOf(BagOfValues bag)
	{
		for(Value v : bag.values){
			if(values.contains(v)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests if this bag is subset of given bag.
	 *
	 * @param bag a bag
	 * @return {@code true} if given bag
	 * is subset if this bag
	 */
	public boolean containsAll(BagOfValues bag){
		Preconditions.checkArgument(type.equals(bag.type));
		return values.containsAll(bag.values);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BagOfValues)){
			return false;
		}
		BagOfValues bag = (BagOfValues)o;
		return type.equals(bag.type) &&
		values.equals(bag.values);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).
		add("DataType", type.getValueType()).
		add("Values", values).toString();
	}

	@Override
	public int hashCode(){
		if(hashCode == 0){
			this.hashCode = Objects.hash(values, type);
		}
		return hashCode;
	}

	/**
	 * A static helper method to retrieve a single
	 * value from a given bag
	 *
	 * @param <T> attribute expression type
	 * @param v a bag of values
	 * @return a single value or {@code null}
	 * if a given bag is {@code null} or empty
	 */
	public static <T extends Value> T value(BagOfValues v){
		return v.<T>single().orElse(null);
	}

	@Override
	public void accept(ExpressionVisitor expv) {
		BagOfAttributeVisitor v = (BagOfAttributeVisitor) expv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	public interface BagOfAttributeVisitor extends ExpressionVisitor
	{
		default void visitEnter(BagOfValues v){
			v.accept(this);
		}
		default void visitLeave(BagOfValues v){
			v.accept(this);
		}
	}

	private static void assertExpressionType(Value value, BagOfValuesType bagType) {
		if (!value.getEvaluatesTo().equals(bagType.getValueType())) {
			throw new IllegalArgumentException(String.format(
					"Given attribute value=\"%s\" " +
							"can't be used as a value of bag=\"%s\"", value, bagType));
		}
	}

	public static class Builder
	{

		private BagOfValuesType bagType;
		private ImmutableMultiset.Builder<Value> valuesBuilder = ImmutableMultiset.builder();

		Builder(BagOfValuesType type){
			this.bagType = Preconditions.checkNotNull(type);
		}

		Builder(ValueType type){
			this.bagType = Preconditions.checkNotNull(type).bagType();
		}


		public Builder value(Object ...values){
			for(Object v : values){
				this.valuesBuilder.add(bagType.getValueType().of(v));
			}
			return this;
		}

		public Builder values(Iterable<Object> values){
			for(Object v : values){
				this.valuesBuilder.add(bagType.getValueType().of(v));
			}
			return this;
		}

		public Builder attributes(Iterable<Value> values){
			for (Value v : values) {
				assertExpressionType(v, bagType);
			}
			this.valuesBuilder.addAll(values);
			return this;
		}

		public Builder attribute(Value...values){
			for(Value v : values){
				assertExpressionType(v, bagType);
				this.valuesBuilder.add(v);
			}
			return this;
		}

		public BagOfValues build(){
			return new BagOfValues(this);
		}
	}
}
