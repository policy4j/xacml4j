package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReference extends XacmlObject 
	implements Expression, PolicyElement
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
	protected AttributeReference(AttributeCategoryId category, 
			AttributeValueType dataType, boolean mustBePresent){
		Preconditions.checkNotNull(category);
		Preconditions.checkNotNull(dataType);
		this.category = category;
		this.evaluatesTo = dataType.bagOf();
		this.mustBePresent = mustBePresent;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return evaluatesTo;
	}
	
	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 * 
	 * @return {@link AttributeValueType}
	 */
	public AttributeValueType getDataType(){
		return evaluatesTo.getDataType();
	}
	
	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	public AttributeCategoryId getCategory(){
		return category;
	}
	
	/**
	 * Governs whether this reference evaluates 
	 * to an empty bag or {@link EvaluationException}
	 * is thrown during this reference evaluation
	 * 
	 * @return <code>true</code> if attribute
	 * must be present
	 */
	public boolean isMustBePresent(){
		return mustBePresent;
	}

}
