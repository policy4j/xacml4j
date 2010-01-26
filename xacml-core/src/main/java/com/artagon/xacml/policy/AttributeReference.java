package com.artagon.xacml.policy;

import com.artagon.xacml.CategoryId;
import com.artagon.xacml.util.Preconditions;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
abstract class AttributeReference implements Expression
{
	private CategoryId category;
	protected BagOfAttributesType<?> evaluatesTo;
	
	/**
	 * Constructs attribute reference with a given
	 * category and dataType
	 * 
	 * @param category an attribute category
	 * @param dataType attribute reference bag XACML
	 * data type
	 */
	protected AttributeReference(CategoryId category, 
			AttributeType dataType){
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
	 * @return {@link AttributeType}
	 */
	public final AttributeType getDataType(){
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
