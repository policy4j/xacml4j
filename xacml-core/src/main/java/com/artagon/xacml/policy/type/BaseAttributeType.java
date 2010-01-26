package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.AttributeValueType;
import com.artagon.xacml.policy.AttributeValue;
import com.artagon.xacml.policy.BagOfAttributeValuesType;
import com.artagon.xacml.util.Objects;
import com.artagon.xacml.util.Preconditions;

/**
 * A base class for all XACML attributes.
 * 
 * @author Giedrius Trumpickas
 */
abstract class BaseAttributeType<V extends AttributeValue> implements AttributeValueType
{
	private String typeId;
	private Class<?> valueClazz;
	private BagOfAttributeValuesType<V> bagType;
	
	/**
	 * Constructs attribute type with given type identifier.
	 * 
	 * @param typeId an attribute type identifier
	 */
	protected BaseAttributeType(String typeId, Class<?> valueType){
		Preconditions.checkNotNull(typeId);
		Preconditions.checkNotNull(valueType);
		this.typeId = typeId;		
		this.valueClazz = valueType;
		this.bagType = new BagOfAttributeValuesType<V>(this);
	}
	
	@Override
	public boolean isConvertableFrom(Object any){
		return valueClazz.isInstance(any) || String.class.isInstance(any);
	}
	
	@Override
	public final Class<?> getValueClass() {
		return valueClazz;
	}

	public final String getDataTypeId(){
		return typeId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(typeId, valueClazz);
	}
		
	@Override
	public final boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeValueType)){
			return false;
		}
		AttributeValueType t = (AttributeValueType)o;
		return typeId.equals(t.getDataTypeId()) && 
			valueClazz.equals(t.getValueClass());
	}
	
	@Override
	public final BagOfAttributeValuesType<V> bagOf(){
		return bagType;
	}
	
	@Override
	public String toString(){
		return getDataTypeId().toString();
	}
}
