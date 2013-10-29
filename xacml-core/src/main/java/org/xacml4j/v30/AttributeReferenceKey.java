package org.xacml4j.v30;

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
			AttributeReferenceBuilder<?> b){
		Preconditions.checkNotNull(b.category);
		Preconditions.checkNotNull(b.dataType);
		this.category = b.category;
		this.dataType = b.dataType;
	}

	public final AttributeCategory getCategory(){
		return category;
	}

	public final AttributeExpType getDataType(){
		return dataType;
	}

	public abstract BagOfAttributeExp resolve(
			EvaluationContext context) throws EvaluationException;

	public static abstract class AttributeReferenceBuilder<T>
	{
		private AttributeCategory category;
		private AttributeExpType dataType;

		public T category(AttributeCategory category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return getThis();
		}

		public T category(String category) throws XacmlSyntaxException
		{
			return category(AttributeCategories.parse(category));
		}

		public T dataType(AttributeExpType type){
			Preconditions.checkNotNull(type);
			this.dataType = type;
			return getThis();
		}

		protected abstract T getThis();
	}

}
