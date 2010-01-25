package com.artagon.xacml.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

/**
 * Represents a XACML bag of attributes type.
 * 
 * @author Giedrius Trumpickas
 *
 * @param <ContentType>
 */
public final class BagOfAttributesType<VT extends Attribute> implements ValueType
{
	private AttributeType type;
	
	/**
	 * Constructs bag of attributes types with a given
	 * attribute type.
	 * 
	 * @param type an attribute type
	 */
	BagOfAttributesType(AttributeType type){
		Preconditions.checkNotNull(type);
		this.type = type;
	}
	
	/**
	 * Gets bag attribute type.
	 * 
	 * @return bag attribute type
	 */
	public AttributeType getDataType(){
		return type;
	}
	
	public String getDataTypeId(){
		return type.getDataTypeId();
	}
	
	/**
	 * Creates bag from given collection of attributes.
	 * 
	 * @param attr a collection of attributes
	 * @return {@link BagOfAttributes} containing given attributes
	 */
	public BagOfAttributes<VT> createFromAttributes(
			Collection<Attribute> attr){
		return new BagOfAttributes<VT>(this, attr);
	}
	
	/**
	 * Creates an empty bag.
	 * 
	 * @return instance of {@link BagOfAttributes} with
	 * no {@link BaseAttribute} instances
	 */
	public  BagOfAttributes<VT> createEmpty(){
		return new BagOfAttributes<VT>(this, Collections.<Attribute>emptyList());
	}
	
	/**
	 * Creates bag from given array of attributes.
	 * 
	 * @param attr an array of attributes
	 * @return {@link BagOfAttributes} containing given attributes
	 */
	public BagOfAttributes<VT> createFromAttributes(Attribute ...attr){
		return new BagOfAttributes<VT>(this, attr);
	}
	
	/**
	 * Creates {@link BagOfAttributes} from a given collection
	 * of attribute values
	 * 
	 * @param values a collection of attribute values
	 * @return {@link BagOfAttributes}
	 */
	public BagOfAttributes<VT> createFromAttributeValues(Collection<?> values)
	{
		Collection<Attribute> attr = new LinkedList<Attribute>();
		for(Object v : values){
			attr.add(type.create(v));
		}
		return createFromAttributes(attr);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof BagOfAttributesType<?>)){
			return false;
		}
		BagOfAttributesType<?> type = (BagOfAttributesType<?>)o;
		return type.getDataType().equals(getDataType());
	}
	
	@Override
	public int hashCode(){
		return type.hashCode();
	}
	
	@Override
	public final String toString(){
		StringBuilder b = new StringBuilder();
		b.append(getClass().getName()).append("[").append(getDataType().toString()).append("]");
		return b.toString();
	}
}
