package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.XacmlObject;

public class DefaultAttributeAssignment extends XacmlObject implements AttributeAssignment
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
	public DefaultAttributeAssignment(String attributeId, 
			AttributeCategoryId category, String issuer, AttributeValue value)
		throws PolicySyntaxException
	{
		checkNotNull(attributeId, "Attribute id can't be null");
		checkNotNull(category, "Attribute category can't be null");
		checkNotNull(value, "Attribute value can't be null");
		this.attributeId = attributeId;
		this.category = category;
		this.issuer = issuer;
		this.attribute = value;
	}
	
	@Override
	public String getAttributeId(){
		return attributeId;
	}
	
	@Override
	public AttributeValue getAttribute(){
		return attribute;
	}
	
	@Override
	public AttributeCategoryId getCategory(){
		return category;
	}
	
	@Override
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
