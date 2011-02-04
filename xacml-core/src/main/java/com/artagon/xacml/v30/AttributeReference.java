package com.artagon.xacml.v30;



/**
 * A base class for XACML attribute references
 * in the XACML policies
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReference  implements Expression
{
	private boolean mustBePresent;
	
	/**
	 * Constructs attribute reference with a given
	 * category and dataType
	 * 
	 * @param category an attribute category
	 * @param dataType attribute reference bag XACML
	 * data type
	 */
	protected AttributeReference(boolean mustBePresent){
		this.mustBePresent = mustBePresent;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return getReferenceKey().getDataType().bagType();
	}
	
	public abstract AttributeReferenceKey getReferenceKey();
	
	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 * 
	 * @return {@link AttributeValueType}
	 */
	public AttributeValueType getDataType(){
		return getReferenceKey().getDataType();
	}
	
	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	public AttributeCategory getCategory(){
		return getReferenceKey().getCategory();
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

	@Override
	public abstract BagOfAttributeValues evaluate(EvaluationContext context) 
		throws EvaluationException;

}
