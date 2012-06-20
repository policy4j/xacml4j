package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


public class AttributeAssignment 
{
	private AttributeExp attribute;
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
			AttributeExp value)
	{
		Preconditions.checkNotNull(attributeId, "Attribute id can't be null");
		Preconditions.checkNotNull(value, "Attribute value can't be null");
		this.attributeId = attributeId;
		this.category = category;
		this.issuer = issuer;
		this.attribute = value;
	}
	
	public AttributeAssignment(
			String attributeId, 
			AttributeExp value)
	{
		this(attributeId, null, null, value);
	}
	
	public AttributeAssignment(
			String attributeId, 
			String issuer,
			AttributeExp value)
	{
		this(attributeId, null, null, value);
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
	public AttributeExp getAttribute(){
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
	
	@Override
	public int hashCode(){
		return Objects.hashCode(
				attributeId, 
				category, 
				attribute, 
				issuer);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributeId", attributeId)
		.add("category", category)
		.add("value", attribute)
		.add("issuer", issuer).toString();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeAssignment)){
			return false;
		}
		AttributeAssignment a = (AttributeAssignment)o;
		return attributeId.equals(a.attributeId) &&
			attribute.equals(a.attribute) && 
			Objects.equal(category, a.category) &&
			Objects.equal(issuer, a.issuer);
	}
}
