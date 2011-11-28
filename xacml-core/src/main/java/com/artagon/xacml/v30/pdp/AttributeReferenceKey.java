package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeCategory;
import com.google.common.base.Preconditions;

/**
 * A base class for attribute references
 * 
 * @author Giedrius Trumpickas
 */
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
	
	public abstract BagOfAttributeExp resolve(
			EvaluationContext context) throws EvaluationException;
}
