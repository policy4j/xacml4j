package com.artagon.xacml.v30.spi.pip;

import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.XacmlObject;
import com.google.common.base.Preconditions;

public final class AttributeDescriptor extends XacmlObject
{
	private String attributeId;
	private AttributeExpType dataType;
	private BagOfAttributesExp defaultValue;
	
	/**
	 * Constructs attribute descriptor
	 * 
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 */
	public AttributeDescriptor(String attributeId, 
			AttributeExpType dataType)
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
	 * Gets attribute defal
	 * @return
	 */
	public BagOfAttributesExp getDefaultValue(){
		return defaultValue;
	}
	
	/**
	 * Gets expected attribute data type
	 * 
	 * @return {@link AttributeExpType} an
	 * attribute data type
	 */
	public AttributeExpType getDataType(){
		return dataType;
	}
}


