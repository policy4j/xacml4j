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

import org.xacml4j.v30.pdp.AttributeAssignmentExpression;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class AttributeAssignment
{
	private final AttributeExp attribute;
	private final CategoryId category;
	private final String attributeId;
	private final String issuer;

	/**
	 * Creates attribute assignment with a
	 * given attribute identifier
	 *
	 * @param b attribute assignment builder
	 */
	private AttributeAssignment(Builder b)
	{
		Preconditions.checkNotNull(b.attributeId, "Attribute id can't be null");
		Preconditions.checkNotNull(b.value, "Attribute value can't be null");
		this.attributeId = b.attributeId;
		this.category = b.category;
		this.issuer = b.issuer;
		this.attribute = b.value;
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets attribute identifier
	 *
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

	/**
	 * Gets attribute value
	 *
	 * @return attribute value
	 */
	public AttributeExp getAttribute(){
		return attribute;
	}

	/**
	 * Gets attribute category
	 *
	 * @return attribute category
	 */
	public CategoryId getCategory(){
		return category;
	}


	/**
	 * Gets attribute issuer identifier
	 *
	 * @return attribute issuer
	 */
	public String getIssuer(){
		return issuer;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				attributeId,
				category,
				attribute,
				issuer);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributeId", attributeId)
		.add("category", category)
		.add("value", attribute)
		.add("issuer", issuer).toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeAssignment)){
			return false;
		}
		AttributeAssignment a = (AttributeAssignment)o;
		return attributeId.equals(a.attributeId) &&
			attribute.equals(a.attribute) &&
			Objects.equal(category, a.category) &&
			Objects.equal(issuer, a.issuer);
	}

	public static class Builder
	{
		private String attributeId;
		private CategoryId category;
		private String issuer;
		private AttributeExp value;

		public Builder id(String attributeId){
			Preconditions.checkNotNull(attributeId);
			this.attributeId = attributeId;
			return this;
		}

		/**
		 * Copies all state from a given {@link AttributeAssignmentExpression}
		 * except attribute value expression
		 *
		 * @param attrAssigExp attribute assignment expression
		 * @return {@link Builder}
		 */
		public Builder from(AttributeAssignmentExpression attrAssigExp)
		{
			this.attributeId = attrAssigExp.getAttributeId();
			this.category = attrAssigExp.getCategory();
			this.issuer = attrAssigExp.getIssuer();
			return this;
		}

		public Builder category(CategoryId category){
			this.category = category;
			return this;
		}

		public Builder category(String category){
			this.category = !Strings.isNullOrEmpty(category)?Categories.parse(category):null;
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer =  issuer;
			return this;
		}

		public Builder value(AttributeExp v){
			Preconditions.checkNotNull(v);
			this.value = v;
			return this;
		}

		public AttributeAssignment build(){
			return new AttributeAssignment(this);
		}
	}
}
