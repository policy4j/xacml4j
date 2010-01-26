package com.artagon.xacml.policy;

import com.artagon.xacml.CategoryId;
import com.artagon.xacml.util.Preconditions;

public class AttributeAssignment implements PolicyElement
{
	private Attribute attribute;
	private CategoryId category;
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
	public AttributeAssignment(String attributeId, 
			CategoryId category, String issuer, Attribute value){
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(value);
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
	public Attribute getAttribute(){
		return attribute;
	}
	
	/**
	 * Gets attribute category
	 * 
	 * @return attribute category
	 */
	public CategoryId getCategory(){
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
