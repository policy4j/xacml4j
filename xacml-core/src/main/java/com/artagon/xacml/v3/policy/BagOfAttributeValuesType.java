package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;

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
	
	public String getDataTypeId(){
		return type.getDataTypeId();
	}
	
	/**
	 * Creates bag from given collection of attributes.
	 * 
	 * @param attr a collection of attributes
	 * @return {@link BagOfAttributeValues} containing given attributes
	 */
	public BagOfAttributeValues<VT> createFromAttributes(
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
	public BagOfAttributeValues<VT> createFromAttributes(AttributeValue ...attr){
		return new BagOfAttributeValues<VT>(this, attr);
	}
	
	/**
	 * Creates {@link BagOfAttributeValues} from a given collection
	 * of attribute values
	 * 
	 * @param values a collection of attribute values
	 * @return {@link BagOfAttributeValues}
	 */
	public BagOfAttributeValues<VT> createFromAttributeValues(Collection<?> values)
	{
		Collection<AttributeValue> attr = new LinkedList<AttributeValue>();
		for(Object v : values){
			attr.add(type.create(v));
		}
		return createFromAttributes(attr);
	}
}
