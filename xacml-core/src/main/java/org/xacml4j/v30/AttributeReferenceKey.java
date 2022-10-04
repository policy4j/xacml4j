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

import org.xacml4j.v30.types.XacmlTypes;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * A base class for attribute references
 *
 * @author Giedrius Trumpickas
 */
public abstract class AttributeReferenceKey
{
	protected final Optional<CategoryId> category;
	protected final ValueType dataType;

	protected AttributeReferenceKey(
			Builder<?> b){
		this.category = Optional.ofNullable(b.category);
		this.dataType = Objects.requireNonNull(b.dataType);
	}

	public final CategoryId getCategory(){
		return category.get();
	}

	public final ValueType getDataType(){
		return dataType;
	}


	public static abstract class Builder<T>
	{
		private CategoryId category;
		private ValueType dataType;


		public T category(String category) {
			this.category = CategoryId.of(category);
			return getThis();
		}

		public T category(Value category) {
			this.category = CategoryId.of(category);
			return getThis();
		}

		public T category(CategoryId category) {
			this.category = Objects.requireNonNull(category, "category");
			return getThis();
		}

		public T category(URI category) {
			this.category = CategoryId.of(category);
			return getThis();
		}

		public T dataType(Object typeId){
			this.dataType = XacmlTypes.getType(typeId)
					.orElseThrow(()-> SyntaxException.invalidDataTypeId(typeId));
			return getThis();
		}

		protected abstract T getThis();
	}

}
