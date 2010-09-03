package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeValueType;
import com.google.common.base.Preconditions;

public final class AttributeDescriptor 
{
	private String attributeId;
	private AttributeValueType dataType;
	
	/**
	 * Constructs attribute descriptor
	 * 
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 */
	public AttributeDescriptor(String attributeId, 
			AttributeValueType dataType)
	{
		Preconditions.checkArgument(attributeId != null);
		Preconditions.checkArgument(dataType != null);
		this.attributeId = attributeId;
		this.dataType = dataType;
	}
		
	/**
	 * Gets an attribute identifier
	 * 
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/**
	 * Gets expected attribute data type
	 * 
	 * @return {@link AttributeValueType} an
	 * attribute data type
	 */
	public AttributeValueType getDataType(){
		return dataType;
	}
}


