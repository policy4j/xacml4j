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
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

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
public final class BagOfAttributeValues
	implements ValueExpression, Serializable
{
	private static final long serialVersionUID = -8197446176793438616L;

	private final BagOfAttributeValuesType type;
	private final Multiset<AttributeValue> values;
	private int hashCode;
	
	/**
	 * Constructs bag of attributes.
	 *
	 * @param type a bag attribute type
	 * @param attributes a collection of attributes
	 */
	private BagOfAttributeValues(Builder b){

		this.type = b.bagType;
		this.values = b.valuesBuilder.build();
	}

	@SuppressWarnings("unchecked")
	public <T extends AttributeValue> Collection<T> values() {
		return (Collection<T>)values;
	}

	public Stream<AttributeValue> stream(){
		return values.stream();
	}

	/**
	 * Gets bag value data type
	 *
	 * @return {@link AttributeValueType}
	 */
	public AttributeValueType getDataType(){
		return type.getDataType();
	}

	@Override
	public BagOfAttributeValuesType getEvaluatesTo() {
		return type;
	}

	/**
	 * Gets first attribute value
	 * in this bag
	 *
	 * @return first {@link AttributeValue}
	 * defaultProvider in this bag
	 * @exception NoSuchElementException if bag is empty
	 */
	public <T extends AttributeValue> T value(){
		return this.<T>values().iterator().next();
	}

	/**
	 * Gets number of {@link AttributeValue}
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
	public boolean contains(AttributeValue attr){
		return values.contains(attr);
	}

	/**
	 * Returns the number of elements in this bag equal
	 * to the specified attribute value.
	 *
	 * @param value an attribute value
	 * @return a number of elements equal to the
	 * specified value
	 */
	public int count(AttributeValue value){
		return values.count(value);
	}

	/**
	 * Returns a bag of such that it contains all elements of
	 * all the argument bags all duplicate elements are removed
	 *
	 * @param bag an another bag
	 * @return union of this and given bag without duplicate
	 * elements
	 */
	public BagOfAttributeValues union(BagOfAttributeValues bag)
	{
		Preconditions.checkArgument(type.equals(bag.type));
		return type.builder()
				.attributes(bag.values)
				.attributes(values).build();
	}

	/**
	 * Returns a bag of values such that it contains only
	 * elements that are common between this and given bag
	 *
	 * @param bag an another bag
	 * @return bag which contains common
	 * elements between this and given bag
	 */
	public BagOfAttributeValues intersection(BagOfAttributeValues bag)
	{
		Preconditions.checkArgument(type.equals(bag.type));
		BagOfAttributeValues.Builder b = type.builder();
		for(AttributeValue attr : values){
			if(bag.values.contains(attr)){
				b.attribute(attr);
			}
		}
		return b.build();
	}

	/**
	 * Tests if this bag contains at least on value
	 * from the given bag
	 *
	 * @param bag a bag
	 * @return {@code true} if this bag contains
	 * at least one value from the given bag
	 */
	public boolean containsAtLeastOneOf(BagOfAttributeValues bag)
	{
		for(AttributeValue v : bag.values){
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
	public boolean containsAll(BagOfAttributeValues bag){
		Preconditions.checkArgument(type.equals(bag.type));
		return values.containsAll(bag.values);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BagOfAttributeValues)){
			return false;
		}
		BagOfAttributeValues bag = (BagOfAttributeValues)o;
		return type.equals(bag.type) &&
		values.equals(bag.values);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).
		add("DataType", type.getDataType()).
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
	public static <T extends AttributeValue> T value(BagOfAttributeValues v){
		if(v == null ||
				v.isEmpty()){
			return null;
		}
		return v.value();
	}

	@Override
	public void accept(ExpressionVisitor expv) {
		BagOfAttributeVisitor v = (BagOfAttributeVisitor)expv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	public interface BagOfAttributeVisitor extends ExpressionVisitor
	{
		void visitEnter(BagOfAttributeValues v);
		void visitLeave(BagOfAttributeValues v);
	}

	private static void assertExpressionType(AttributeValue value, BagOfAttributeValuesType bagType) {
		if (!value.getType().equals(bagType.getDataType())) {
			throw new IllegalArgumentException(String.format(
					"Given attribute value=\"%s\" " +
							"can't be used as a value of bag=\"%s\"", value, bagType));
		}
	}

	public static class Builder
	{

		private BagOfAttributeValuesType bagType;
		private ImmutableMultiset.Builder<AttributeValue> valuesBuilder = ImmutableMultiset.builder();

		Builder(BagOfAttributeValuesType type){
			this.bagType = Preconditions.checkNotNull(type);
		}

		Builder(AttributeValueType type){
			this.bagType = Preconditions.checkNotNull(type).bagType();
		}


		public Builder value(Object ...values){
			for(Object v : values){
				this.valuesBuilder.add(bagType.getDataType().of(v));
			}
			return this;
		}

		public Builder values(Iterable<Object> values){
			for(Object v : values){
				this.valuesBuilder.add(bagType.getDataType().of(v));
			}
			return this;
		}

		public Builder attributes(Iterable<AttributeValue> values){
			for (AttributeValue v : values) {
				assertExpressionType(v, bagType);
			}
			this.valuesBuilder.addAll(values);
			return this;
		}

		public Builder attribute(AttributeValue...values){
			for(AttributeValue v : values){
				assertExpressionType(v, bagType);
				this.valuesBuilder.add(v);
			}
			return this;
		}

		public BagOfAttributeValues build(){
			return new BagOfAttributeValues(this);
		}
	}
}
