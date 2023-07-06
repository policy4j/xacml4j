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
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.xacml4j.v30.types.Value;


/**
 * XACML attribute assigment
 *
 * @author Giedrius Trumpickasd
 */
public final class AttributeAssignment
{
	private final Value attribute;
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

	public static Builder builder(String attributeId){
		return new Builder().attributeId(attributeId);
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
	public Value getAttribute(){
		return attribute;
	}

	/**
	 * Gets attribute category
	 *
	 * @return attribute category
	 */
	public Optional<CategoryId> getCategory(){
		return Optional.ofNullable(category);
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
		return MoreObjects.toStringHelper(this)
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
		private Value value;

		public Builder attributeId(String attributeId){
			Preconditions.checkNotNull(attributeId);
			this.attributeId = attributeId;
			return this;
		}

		public Builder category(String category) {
			if(category != null){
				this.category = CategoryId.of(category);
			}
			return this;
		}

		public Builder category(Value category) {
			if(category != null){
				this.category = CategoryId.of(category);
			}
			return this;
		}

		public Builder category(CategoryId category) {
			this.category = category;
			return this;
		}

		public Builder category(URI category) {
			if(category != null){
				this.category = CategoryId.of(category);
			}
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer =  issuer;
			return this;
		}

		public Builder value(Value v){
			this.value = v;
			return this;
		}

		public AttributeAssignment build(){
			return new AttributeAssignment(this);
		}
	}
}
