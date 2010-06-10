package com.artagon.xacml.v3;



public class AttributeAssignment extends XacmlObject implements PolicyElement
{
	private AttributeValue attribute;
	private AttributeCategoryId category;
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
	 * @exception PolicySyntaxException if attribute assignment can not
	 * be created with a given values
	 */
	public AttributeAssignment(
			String attributeId, 
			AttributeCategoryId category, 
			String issuer, 
			AttributeValue value)
		throws PolicySyntaxException
	{
		checkNotNull(attributeId, "Attribute id can't be null");
		checkNotNull(value, "Attribute value can't be null");
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
	public AttributeCategoryId getCategory(){
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
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		attribute.accept(v);
		v.visitLeave(this);
	}
}
