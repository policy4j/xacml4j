package com.artagon.xacml.v30;

import com.google.common.base.Preconditions;


public class AttributeAssignment 
	extends XacmlObject
{
	private AttributeValue attribute;
	private AttributeCategory category;
	private String attributeId;
	private String issuer;
	
	/**
	 * Creates attribute assignment with a 
	 * given attribute identifier
	 * 
	 * @param attributeId an attribute id
	 * @param category an attribute category
	 * @param issuer an attribute issuer
	 * @param value an attribute value
	 */
	public AttributeAssignment(
			String attributeId, 
			AttributeCategory category, 
			String issuer, 
			AttributeValue value)
	{
		Preconditions.checkNotNull(attributeId, "Attribute id can't be null");
		Preconditions.checkNotNull(value, "Attribute value can't be null");
		this.attributeId = attributeId;
		this.category = category;
		this.issuer = issuer;
		this.attribute = value;
	}
	
	/**
	 * Gets attribute identifier
	 * 
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/**
	 * Gets attribute value
	 * 
	 * @return attribute value
	 */
	public AttributeValue getAttribute(){
		return attribute;
	}
	
	/**
	 * Gets attribute category
	 * 
	 * @return attribute category
	 */
	public AttributeCategory getCategory(){
		return category;
	}
	
	
	/**
	 * Gets attribute issuer identifier
	 * 
	 * @return attribute issuer
	 */
	public String getIssuer(){
		return issuer;
	}
}
