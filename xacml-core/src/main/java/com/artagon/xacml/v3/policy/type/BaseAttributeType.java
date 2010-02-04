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
abstract class BaseAttributeType<V extends AttributeValue> extends XacmlObject 
	implements AttributeValueType
{
	private String typeId;
	private BagOfAttributeValuesType<V> bagType;
	
	/**
	 * Constructs attribute type with given type identifier.
	 * 
	 * @param typeId an attribute type identifier
	 */
	protected BaseAttributeType(String typeId)
	{
		Preconditions.checkNotNull(typeId);
		this.typeId = typeId;		
		this.bagType = new BagOfAttributeValuesType<V>(this);
	}
		
	public final String getDataTypeId(){
		return typeId;
	}
	
	@Override
	public final BagOfAttributeValuesType<V> bagOf(){
		return bagType;
	}
}
