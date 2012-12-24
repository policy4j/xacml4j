package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueType;



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
	 * @return {@link AttributeExpType}
	 */
	public AttributeExpType getDataType(){
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
	public abstract BagOfAttributeExp evaluate(EvaluationContext context) 
		throws EvaluationException;

}
