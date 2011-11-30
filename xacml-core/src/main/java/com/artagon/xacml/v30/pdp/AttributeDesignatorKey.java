package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeCategory;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class AttributeDesignatorKey 
	extends AttributeReferenceKey
{
	private String attributeId;
	private String issuer;
	private int hashCode;
	
	public AttributeDesignatorKey(
			AttributeCategory category, 
			String attributeId, 
			AttributeExpType dataType, 
			String issuer){
		super(category, dataType);
		Preconditions.checkNotNull(attributeId);
		this.attributeId = attributeId;
		this.issuer = issuer;
		this.hashCode = Objects.hashCode(
				category, attributeId, dataType, issuer);
	}
	
	public AttributeDesignatorKey createWithIssuer(String issuer){
		return new AttributeDesignatorKey(getCategory(), 
				getAttributeId(), getDataType(), issuer);
	}
	
	public String getAttributeId(){
		return attributeId;
	}
	
	
	public String getIssuer(){
		return issuer;
	}
	
	@Override
	public BagOfAttributeExp resolve(EvaluationContext context)
			throws EvaluationException 
	{
		return context.resolve(this);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("Category", getCategory())
		.add("AttributeId", attributeId)
		.add("DataType", getDataType())
		.add("Issuer", issuer).toString();
	}
	
	@Override
	public int hashCode(){
		return hashCode;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeDesignatorKey)){
			return false;
		}
		AttributeDesignatorKey s = (AttributeDesignatorKey)o;
		return category.equals(s.category) &&
		dataType.equals(s.dataType) && attributeId.equals(s.attributeId) &&
		Objects.equal(issuer, s.issuer);
	}
}
