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
public abstract class AttributeReference implements Expression
{
	private final boolean mustBePresent;

	/**
	 * Constructs attribute reference
	 *
	 * @param b attribute reference builder
	 * data type
	 */
	protected AttributeReference(Builder b){
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
	 * @return {@code true} if attribute
	 * must be present
	 */
	public boolean isMustBePresent(){
		return mustBePresent;
	}

	@Override
	public abstract BagOfAttributeExp evaluate(EvaluationContext context)
		throws EvaluationException;

	public static abstract class Builder<T>
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
		protected abstract AttributeReferenceKey.Builder<?> getBuilder();
	}
}
