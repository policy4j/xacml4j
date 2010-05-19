package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeAssignment;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.google.common.base.Preconditions;

public class DefaultAttributeAssignment implements AttributeAssignment
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
	 */
	public DefaultAttributeAssignment(String attributeId, 
			AttributeCategoryId category, String issuer, AttributeValue value){
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(value);
		this.attributeId = attributeId;
		this.category = category;
		this.issuer = issuer;
		this.attribute = value;
	}
	
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeAssignment#getAttributeId()
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeAssignment#getAttribute()
	 */
	public AttributeValue getAttribute(){
		return attribute;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeAssignment#getCategory()
	 */
	public AttributeCategoryId getCategory(){
		return category;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeAssignment#getIssuer()
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
