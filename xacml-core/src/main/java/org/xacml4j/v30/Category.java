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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


public class Category
{
	private final String id;
	private final CategoryId categoryId;
	private final CategoryReference ref;
	private final Entity entity;

	/**
	 * Creates {@link Category} from given {@link Category.Builder}
	 *
	 * @param b a category builder
	 */
	private Category(Builder b) {
		Preconditions.checkNotNull(b.category);
		Preconditions.checkNotNull(b.entity);
		this.id = b.id;
		this.categoryId = b.category;
		this.entity = b.entity;
		this.ref = (b.id == null)
				? null
				: CategoryReference.builder().id(b.id).build();
	}

	/**
	 * Constructs {@link Category.Builder} for given
	 * attribute category
	 *
	 * @param category attribute category
	 * @return {@link Category.Builder} instance
	 */
	public static Builder builder(CategoryId category){
		return new Builder(category);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * An unique identifier of the attribute in
	 * the request context
	 *
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	public String getId(){
		return id;
	}

	public CategoryReference getReference(){
		return ref;
	}

	public Entity getEntity(){
		return entity;
	}

	/**
	 * Gets an attribute category
	 *
	 * @return attribute category
	 */
	public CategoryId getCategoryId(){
		return categoryId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("category", categoryId)
		.add("id", id)
		.add("entity", entity)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(categoryId, id, entity);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Category)){
			return false;
		}
		Category a = (Category)o;
		return Objects.equal(categoryId, a.categoryId) &&
		Objects.equal(id, a.id) && entity.equals(a.entity);
	}

	public static class Builder
	{
		private String id;
		private CategoryId category;
		private Entity entity;

		private Builder(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
		}

		private Builder(){
		}

		public Builder entity(Entity entity){
			this.entity = entity;
			return this;
		}

		public Builder copyOf(Category a){
			return copyOf(a, Predicates.<Attribute>alwaysTrue());
		}

		public Builder copyOf(Category a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			id(a.getId());
			category(a.getCategoryId());
			entity(Entity.builder().copyOf(a.entity, f).build());
			return this;
		}

		public Builder id(String id){
			this.id = id;
			return this;
		}

		public Builder category(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return this;
		}

		public Category build(){
			return new Category(this);
		}
	}
}
