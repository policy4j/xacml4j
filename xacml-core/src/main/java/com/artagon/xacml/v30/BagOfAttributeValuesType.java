package com.artagon.xacml.v30;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a XACML bag of attributes type.
 * 
 * @author Giedrius Trumpickas
 *
 * @param <ContentType>
 */
public final class BagOfAttributeValuesType implements ValueType
{
	private static final long serialVersionUID = 1317103379388105997L;
	
	private AttributeValueType type;
	
	/**
	 * Constructs bag of attributes types with a given
	 * attribute type.
	 * 
	 * @param type an attribute type
	 */
	public BagOfAttributeValuesType(AttributeValueType type){
		Preconditions.checkNotNull(type);
		this.type = type;
	}
	
	/**
	 * Gets bag attribute type.
	 * 
	 * @return bag attribute type
	 */
	public AttributeValueType getDataType(){
		return type;
	}
	
	/**
	 * Creates bag from given collection of attributes.
	 * 
	 * @param attr a collection of attributes
	 * @return {@link BagOfAttributeValues} containing given attributes
	 */
	public BagOfAttributeValues create(
			Collection<AttributeValue> attr){
		return new BagOfAttributeValues(this, attr);
	}
	
	/**
	 * Creates an empty bag.
	 * 
	 * @return instance of {@link BagOfAttributeValues} with
	 * no {@link BaseAttribute} instances
	 */
	public  BagOfAttributeValues createEmpty(){
		return new BagOfAttributeValues(this, Collections.<AttributeValue>emptyList());
	}
	
	/**
	 * Creates bag from given array of attributes.
	 * 
	 * @param attr an array of attributes
	 * @return {@link BagOfAttributeValues} containing given attributes
	 */
	public BagOfAttributeValues create(AttributeValue ...attr){
		return new BagOfAttributeValues(this, attr);
	}
	
	/**
	 * Creates a bag from a given array
	 * of attribute values
	 * 
	 * @param values an array of attribute values
	 * @return {@link BagOfAttributeValues}
	 */
	public BagOfAttributeValues bagOf(Object ...values){
		if(values == null || 
				values.length == 0){
			return createEmpty();
		}
		Collection<AttributeValue> attrs = new ArrayList<AttributeValue>(values.length);
		for(int  i = 0; i < values.length; i++){
			attrs.add(type.create(values[i]));
		}
		return create(attrs);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof BagOfAttributeValuesType)){
			return false;
		}
		BagOfAttributeValuesType bt = (BagOfAttributeValuesType)o;
		return type.equals(bt.type);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(type);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("TypeId", type.getDataTypeId())
		.toString();
	}
}
