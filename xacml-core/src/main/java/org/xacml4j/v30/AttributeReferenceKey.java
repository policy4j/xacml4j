package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.URI;

import com.google.common.base.Preconditions;

/**
 * A base class for category references
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

		public T category(URI category) 
				throws XacmlSyntaxException{
			return category(Categories.parse(category));
		}
		
		public T category(String category) 
				throws XacmlSyntaxException{
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
