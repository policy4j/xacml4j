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
import com.google.common.base.Strings;

public final class AttributeDesignatorKey
	extends AttributeReferenceKey
{
	private final String attributeId;
	private final String issuer;
	private final int hashCode;

	public AttributeDesignatorKey(Builder b){
		super(b);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(b.attributeId));
		this.attributeId = b.attributeId;
		this.issuer = b.issuer;
		this.hashCode = Objects.hashCode(
				category, attributeId, dataType, issuer);
	}

	public static Builder builder(){
		return new Builder();
	}

	public AttributeDesignatorKey withIssuer(String issuer){
		return builder()
				.from(this)
				.issuer(issuer)
				.build();
	}

	public String getAttributeId(){
		return attributeId;
	}


	public String getIssuer(){
		return issuer;
	}

	@Override
	public BagOfAttributeExp resolve(EvaluationContext context)
			throws EvaluationException
	{
		return context.resolve(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("Category", getCategory())
		.add("AttributeId", attributeId)
		.add("DataType", getDataType())
		.add("Issuer", issuer).toString();
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeDesignatorKey)){
			return false;
		}
		AttributeDesignatorKey s = (AttributeDesignatorKey)o;
		return category.equals(s.category) &&
		dataType.equals(s.dataType) && attributeId.equals(s.attributeId) &&
		Objects.equal(issuer, s.issuer);
	}

	public static class Builder extends AttributeReferenceKey.Builder<Builder>
	{
		private String issuer;
		private String attributeId;

		public Builder attributeId(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.attributeId = id;
			return this;
		}

		public Builder from(AttributeDesignatorKey ref){
			category(ref.getCategory());
			dataType(ref.getDataType());
			attributeId(ref.getAttributeId());
			issuer(ref.getIssuer());
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer = Strings.emptyToNull(issuer);
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeDesignatorKey build(){
			return new AttributeDesignatorKey(this);
		}

	}
}
