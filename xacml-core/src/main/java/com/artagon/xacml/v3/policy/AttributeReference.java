package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.CategoryId;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
abstract class AttributeReference implements Expression
{
	private CategoryId category;
	protected BagOfAttributeValuesType<?> evaluatesTo;
	
	/**
	 * Constructs attribute reference with a given
	 * category and dataType
	 * 
	 * @param category an attribute category
	 * @param dataType attribute reference bag XACML
	 * data type
	 */
	protected AttributeReference(CategoryId category, 
			AttributeValueType dataType){
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
	 * @return {@link AttributeValueType}
	 */
	public final AttributeValueType getDataType(){
		return evaluatesTo.getDataType();
	}
	
	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	public final CategoryId getCategory(){
		return category;
	}
}
