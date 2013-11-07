package org.xacml4j.v30.spi.pip;

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Preconditions;

public final class AttributeDescriptor
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
	 * Gets attribute default value
	 * @return attribute default value
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


