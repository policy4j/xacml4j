package com.artagon.xacml.v3;

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
public final class BagOfAttributeValuesType<VT extends AttributeValue> extends XacmlObject 
	implements ValueType
{
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
	public BagOfAttributeValues<VT> create(
			Collection<AttributeValue> attr){
		return new BagOfAttributeValues<VT>(this, attr);
	}
	
	/**
	 * Creates an empty bag.
	 * 
	 * @return instance of {@link BagOfAttributeValues} with
	 * no {@link BaseAttribute} instances
	 */
	public  BagOfAttributeValues<VT> createEmpty(){
		return new BagOfAttributeValues<VT>(this, Collections.<AttributeValue>emptyList());
	}
	
	/**
	 * Creates bag from given array of attributes.
	 * 
	 * @param attr an array of attributes
	 * @return {@link BagOfAttributeValues} containing given attributes
	 */
	public BagOfAttributeValues<VT> create(AttributeValue ...attr){
		return new BagOfAttributeValues<VT>(this, attr);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this).add("TypeId", type.getDataTypeId()).toString();
	}
}
