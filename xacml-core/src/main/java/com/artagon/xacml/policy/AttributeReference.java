package com.artagon.xacml.policy;

import org.oasis.xacml.azapi.constants.AzCategoryId;

import com.artagon.xacml.util.Preconditions;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
abstract class AttributeReference implements Expression
{
	private AzCategoryId category;
	protected BagOfAttributesType<?> evaluatesTo;
	
	/**
	 * Constructs attribute reference with a given
	 * category and dataType
	 * 
	 * @param category an attribute category
	 * @param dataType attribute reference bag XACML
	 * data type
	 */
	protected AttributeReference(AzCategoryId category, 
			AttributeDataType dataType){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(dataType);
		this.category = category;
		this.evaluatesTo = dataType.bagOf();
	}
	
	@Override
	public final ValueType getEvaluatesTo(){
		return evaluatesTo;
	}
	
	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 * 
	 * @return {@link AttributeDataType}
	 */
	public final AttributeDataType getDataType(){
		return evaluatesTo.getDataType();
	}
	
	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	public final AzCategoryId getCategory(){
		return category;
	}
}
