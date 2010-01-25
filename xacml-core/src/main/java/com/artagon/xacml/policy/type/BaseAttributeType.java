package com.artagon.xacml.policy.type;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.AttributeType;
import com.artagon.xacml.policy.BagOfAttributesType;
import com.artagon.xacml.util.Objects;
import com.artagon.xacml.util.Preconditions;

/**
 * A base class for all XACML attributes.
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeType<AttributeValue extends Attribute> implements AttributeType
{
	private String typeId;
	private Class<?> valueClazz;
	private BagOfAttributesType<AttributeValue> bagType;
	
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
		this.bagType = new BagOfAttributesType<AttributeValue>(this);
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
		if(!(o instanceof AttributeType)){
			return false;
		}
		AttributeType t = (AttributeType)o;
		return typeId.equals(t.getDataTypeId()) && 
			valueClazz.equals(t.getValueClass());
	}
	
	@Override
	public final BagOfAttributesType<AttributeValue> bagOf(){
		return bagType;
	}
	
	@Override
	public String toString(){
		return getDataTypeId().toString();
	}
}
