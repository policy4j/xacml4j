package com.artagon.xacml.v30;

import com.artagon.xacml.v30.core.AttributeCategory;
import com.google.common.base.Preconditions;

public abstract class AttributeReferenceKey 
{
	protected AttributeCategory category;
	protected AttributeExpType dataType;
	
	protected AttributeReferenceKey(
			AttributeCategory category, 
			AttributeExpType dataType){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(dataType);
		this.category = category;
		this.dataType = dataType;
	}
	
	public final AttributeCategory getCategory(){
		return category;
	}
	
	public final AttributeExpType getDataType(){
		return dataType;
	}
	
	public abstract BagOfAttributesExp resolve(
			EvaluationContext context) throws EvaluationException;
}
