package com.artagon.xacml.v3.policy.type;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValuesType;

/**
 * A base class for all XACML attributes.
 * 
 * @author Giedrius Trumpickas
 */
abstract class BaseAttributeType<V extends AttributeValue> extends XacmlObject implements AttributeValueType
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
	public final BagOfAttributeValuesType<V> bagOf(){
		return bagType;
	}
}
