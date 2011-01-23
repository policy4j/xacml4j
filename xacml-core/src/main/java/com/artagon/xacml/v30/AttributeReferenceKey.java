package com.artagon.xacml.v30;

import com.google.common.base.Preconditions;

public abstract class AttributeReferenceKey 
{
	protected AttributeCategory category;
	protected AttributeValueType dataType;
	
	protected AttributeReferenceKey(
			AttributeCategory category, 
			AttributeValueType dataType){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(dataType);
		this.category = category;
		this.dataType = dataType;
	}
	
	public final AttributeCategory getCategory(){
		return category;
	}
	
	public final AttributeValueType getDataType(){
		return dataType;
	}
	
	public abstract BagOfAttributeValues resolve(
			EvaluationContext context) throws EvaluationException;
}
