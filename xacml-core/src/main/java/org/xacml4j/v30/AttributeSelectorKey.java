package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class AttributeSelectorKey
	extends AttributeReferenceKey
{
	private String xpath;
	private String contextSelectorId;
	private int hashCode;

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
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeSelectorKey)){
			return false;
		}
		AttributeSelectorKey s = (AttributeSelectorKey)o;
		return category.equals(s.category) &&
		dataType.equals(s.dataType) && xpath.equals(s.xpath) &&
		Objects.equal(contextSelectorId, s.contextSelectorId);
	}

	public static class Builder extends AttributeReferenceBuilder<Builder>
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
