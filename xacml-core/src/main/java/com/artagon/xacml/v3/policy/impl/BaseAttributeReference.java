package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.ValueType;
import com.google.common.base.Preconditions;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
abstract class BaseAttributeReference implements AttributeReference
{
	private boolean mustBePresent;
	private AttributeCategoryId category;
	protected BagOfAttributeValuesType<?> evaluatesTo;
	
	/**
	 * Constructs attribute reference with a given
	 * category and dataType
	 * 
	 * @param category an attribute category
	 * @param dataType attribute reference bag XACML
	 * data type
	 */
	protected BaseAttributeReference(AttributeCategoryId category, 
			AttributeValueType dataType, boolean mustBePresent){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(dataType);
		this.category = category;
		this.evaluatesTo = dataType.bagOf();
		this.mustBePresent = mustBePresent;
	}
	
	@Override
	public final ValueType getEvaluatesTo(){
		return evaluatesTo;
	}
	
	@Override
	public final AttributeValueType getDataType(){
		return evaluatesTo.getDataType();
	}
	
	@Override
	public final AttributeCategoryId getCategory(){
		return category;
	}
	
	@Override
	public final boolean isMustBePresent(){
		return mustBePresent;
	}

}
