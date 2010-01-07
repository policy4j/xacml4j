package com.artagon.xacml.policy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.artagon.xacml.util.Objects;
import com.artagon.xacml.util.Preconditions;

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
public final class BagOfAttributes<T extends Attribute> implements Value
{
	private BagOfAttributesType<T> type;
	private List<T> values = new LinkedList<T>();
	
	/**
	 * Constructs bag of attributes.
	 * 
	 * @param type a bag attribute type
	 * @param attributes a collection of attributes
	 */
	@SuppressWarnings("unchecked")
	BagOfAttributes(BagOfAttributesType<T> type, 
			Collection<Attribute> attributes){		
		for(Attribute attr: attributes){	
			Preconditions.checkArgument(
					attr.getType().equals(type.getDataType()),
					String.format("Only attributes of type=\"%s\" " +
							"are allowed in this bag, given type=\"%s\"", 
					type.getDataType(), attr.getType()));
			values.add((T)attr);
		}	
		this.type = type;
	}
	
	/**
	 * Constructs bag of attributes.
	 * 
	 * @param type a bag attribute type
	 */
	BagOfAttributes(BagOfAttributesType<T> type, 
			Attribute ...attributes){
		this(type, Arrays.asList(attributes));				
	}
	
	public Iterable<T> values() {		
		return values;
	}
	
	/**
	 * Gets bag attribute type
	 * 
	 * @return
	 */
	public AttributeDataType getAttributeType(){
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
	 * @return first {@link Attribute}
	 * instance in this bag
	 * @exception NoSuchElementException if bag is empty
	 */
	public T value(){
		return values().iterator().next();
	}
	
	/**
	 * Gets number of {@link Attribute}
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
	public boolean contains(Attribute attr){
		return values.contains(attr);
	}
	
	public BagOfAttributes<T> join(BagOfAttributes<?> bag)
	{
		Preconditions.checkArgument(getType().equals(bag.getType()));
		Collection<Attribute> join = new ArrayList<Attribute>(values.size() + bag.size());
		join.addAll(values);
		join.addAll(bag.values);
		return type.createFromAttributes(join);
	}
	
	@Override
	public BagOfAttributes<T> evaluate(EvaluationContext context)
			throws PolicyEvaluationException {
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
	public boolean containsAll(BagOfAttributes<?> bag){		
		return values.containsAll(bag.values);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BagOfAttributes<?>)){
			return false;
		}
		BagOfAttributes<?> bag = (BagOfAttributes<?>)o;
		return getType().equals(bag.getType()) 
		&& values.containsAll(bag.values) 
		&& bag.values.containsAll(values);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(values, type);
	}
	
	@Override
	public String toString(){
		return String.format("%s[dataTypeId=\"%s\" size=\"%d\"]", 
				getClass().getName(), getType(), size());
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
