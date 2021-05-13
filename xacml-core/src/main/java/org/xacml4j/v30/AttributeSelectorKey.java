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

public final class AttributeSelectorKey
	extends AttributeReferenceKey
{
	private final String xpath;
	private final String contextSelectorId;
	private final int hashCode;

	private AttributeSelectorKey(Builder b){
		super(b);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(b.xpath));
		this.xpath = b.xpath;
		this.contextSelectorId = b.contextSelectorId;
		this.hashCode = Objects.hashCode(
				category, xpath, dataType, contextSelectorId);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * An XPath expression whose context node is the Content
	 * element of the attribute category indicated by the Category
	 * attribute. There SHALL be no restriction on the XPath syntax,
	 * but the XPath MUST NOT refer to or traverse any content
	 * outside the Content element in any way.
	 *
	 * @return an XPath expression
	 */
	public String getPath(){
		return xpath;
	}

	/**
	 * This attribute id refers to the attribute (by its AttributeId)
	 * in the request context in the category given by the Category attribute.
	 * The referenced attribute MUST have data type
	 * urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression,
	 * and must select a single node in the content element.
	 * The XPathCategory attribute of the referenced attribute MUST
	 * be equal to the Category attribute of the attribute selector
	 *
	 * @return context selector id
	 */
	public String getContextSelectorId(){
		return contextSelectorId;
	}


	@Override
	public BagOfAttributeExp resolve(EvaluationContext context)
			throws EvaluationException {
		return context.resolve(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("Category", getCategory())
		.add("Path", xpath)
		.add("DataType", getDataType())
		.add("ContextSelectorId", contextSelectorId).toString();
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeSelectorKey)){
			return false;
		}
		AttributeSelectorKey s = (AttributeSelectorKey)o;
		return category.equals(s.category) &&
		dataType.equals(s.dataType) &&
		xpath.equals(s.xpath) &&
		Objects.equal(contextSelectorId, s.contextSelectorId);
	}

	public static class Builder extends AttributeReferenceKey.Builder<Builder>
	{
		private String xpath;
		private String contextSelectorId;

		public Builder xpath(String xpath){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(xpath));
			this.xpath = xpath;
			return this;
		}

		public Builder contextSelectorId(String id){
			this.contextSelectorId = Strings.emptyToNull(id);
			return this;
		}

		public Builder from(AttributeSelectorKey s){
			return builder()
					.category(s.category)
					.dataType(s.dataType)
					.xpath(s.xpath)
					.contextSelectorId(s.contextSelectorId);
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeSelectorKey build(){
			return new AttributeSelectorKey(this);
		}
	}
}
