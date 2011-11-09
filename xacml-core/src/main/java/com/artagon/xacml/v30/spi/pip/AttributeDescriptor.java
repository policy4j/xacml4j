package com.artagon.xacml.v30.spi.pip;

import com.artagon.xacml.v30.pdp.AttributeExpType;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.XacmlObject;
import com.google.common.base.Preconditions;

public final class AttributeDescriptor extends XacmlObject
{
	private String attributeId;
	private AttributeExpType dataType;
	private BagOfAttributeExp defaultValue;
	
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
	public BagOfAttributeExp getDefaultValue(){
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


