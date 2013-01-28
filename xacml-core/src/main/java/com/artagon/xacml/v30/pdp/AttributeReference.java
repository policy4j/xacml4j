package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.AttributeReferenceKey;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ValueType;

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
	protected AttributeReference(AttributeReferenceBuilder<?> b){
		this.mustBePresent = b.mustBePresent;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return getReferenceKey().getDataType().bagType();
	}
	
	/**
	 * Gets attribute reference key
	 * 
	 * @return an attribute reference key
	 */
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
	
	public static abstract class AttributeReferenceBuilder<T>
	{
		private boolean mustBePresent = false;
		
		public T category(AttributeCategory category){
			getBuilder().category(category);
			return getThis();
		}
		
		public T category(String category){
			getBuilder().category(category);
			return getThis();
		}
		
		public T dataType(AttributeExpType type){
			getBuilder().dataType(type);
			return getThis();
		}
		
		public T mustBePresent(boolean present){
			this.mustBePresent = present;
			return getThis();
		}
		
		protected abstract T getThis();
		protected abstract AttributeReferenceKey.AttributeReferenceBuilder<?> getBuilder();
	}
}
