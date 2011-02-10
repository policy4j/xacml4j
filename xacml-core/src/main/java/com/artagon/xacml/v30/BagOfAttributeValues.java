package com.artagon.xacml.v30;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

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
	
	private BagOfAttributeValuesType type;
	private Multiset<AttributeValue> values;
	
	/**
	 * Constructs bag of attributes.
	 * 
	 * @param type a bag attribute type
	 * @param attributes a collection of attributes
	 */
	BagOfAttributeValues(BagOfAttributeValuesType type, 
			Collection<AttributeValue> attributes){		
		this.values = LinkedHashMultiset.create(attributes.size());
		for(AttributeValue attr: attributes){	
			Preconditions.checkArgument(
					attr.getType().equals(type.getDataType()),
					String.format("Only attributes of type=\"%s\" " +
							"are allowed in this bag, given type=\"%s\"", 
					type.getDataType(), attr.getType()));
			values.add(attr);
		}	
		this.type = type;
	}
	
	/**
	 * Constructs bag of attributes.
	 * 
	 * @param type a bag attribute type
	 */
	BagOfAttributeValues(BagOfAttributeValuesType type, 
			AttributeValue ...attributes){
		this(type, Arrays.asList(attributes));				
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AttributeValue> Iterable<T> values() {		
		return (Iterable<T>)values;
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
	public ValueType getEvaluatesTo() {
		return type;
	}

	/**
	 * Gets first attribute value
	 * in this bag
	 * 
	 * @return first {@link AttributeValue}
	 * instance in this bag
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
	 * @return <code>true</code> if bag is empty
	 */
	public boolean isEmpty(){
		return values.isEmpty();
	}
	
	/**
	 * Tests if this bag contains given attribute.
	 * 
	 * @param attr an attribute
	 * @return <code>true</code> if bag contains given attribute
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
		Set<AttributeValue> union = new HashSet<AttributeValue>();
		union.addAll(bag.values);
		union.addAll(values);
		return type.create(union);
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
		Set<AttributeValue> intersection = new HashSet<AttributeValue>();
		for(AttributeValue attr : values){
			if(bag.values.contains(attr)){
				intersection.add(attr);
			}
		}
		return type.create(intersection);
	}
	
	/**
	 * Tests if this bag contains at least on value
	 * from the given bag
	 * 
	 * @param bag a bag
	 * @return <code>true</code> if this bag contains
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
	
	@Override
	public BagOfAttributeValues evaluate(EvaluationContext context)
			throws EvaluationException {
		return this;
	}

	@Override
	public ValueType getType() {
		return type;
	}

	/**
	 * Tests if this bag is subset of given bag.
	 * 
	 * @param bag a bag
	 * @return <code>true</code> if given bag 
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
		return Objects.toStringHelper(this).
		add("DataType", type.getDataType()).
		add("Values", values).toString();
	}
	
	@Override
	public int hashCode(){
		 return Objects.hashCode(
				type, 
				values);
	}
	
	/**
	 * A static helper method to retrieve a single
	 * value from a given bag
	 * 
	 * @param <T>
	 * @param v a bag og values
	 * @return a single value or <code>null</code> 
	 * if a given bag is <code>null</code> or empty
	 */
	public static <T extends AttributeValue> T value(BagOfAttributeValues v){
		if(v == null || 
				v.isEmpty()){
			return null;
		}
		return v.<T>value();
	}

	@Override
	public void accept(ExpressionVisitor v) {
		v.visit(this);
	}
}
