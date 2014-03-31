package org.xacml4j.v30;

import com.google.common.base.Preconditions;

/**
 * A base class for attribute references
 *
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReferenceKey
{
	protected final CategoryId category;
	protected final AttributeExpType dataType;

	protected AttributeReferenceKey(
			Builder<?> b){
		Preconditions.checkNotNull(b.category);
		Preconditions.checkNotNull(b.dataType);
		this.category = b.category;
		this.dataType = b.dataType;
	}

	public final CategoryId getCategory(){
		return category;
	}

	public final AttributeExpType getDataType(){
		return dataType;
	}

	public abstract BagOfAttributeExp resolve(
			EvaluationContext context) throws EvaluationException;

	public static abstract class Builder<T>
	{
		private CategoryId category;
		private AttributeExpType dataType;

		public T category(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return getThis();
		}

		public T category(String category) throws XacmlSyntaxException
		{
			return category(Categories.parse(category));
		}

		public T dataType(AttributeExpType type){
			Preconditions.checkNotNull(type);
			this.dataType = type;
			return getThis();
		}

		protected abstract T getThis();
	}

}
