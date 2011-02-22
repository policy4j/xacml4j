package com.artagon.xacml.v30;

import com.google.common.base.Objects;

public final class AttributeSelectorKey 
	extends AttributeReferenceKey 
{
	private String xpath;
	private String contextSelectorId;
	private int hashCode;
	
	public AttributeSelectorKey(AttributeCategory category, 
			String xpath, AttributeValueType dataType, 
			String contextAttributeId){
		super(category, dataType);
		this.xpath = xpath;
		this.contextSelectorId = contextAttributeId;
		this.hashCode = Objects.hashCode(
				category, xpath, dataType, contextAttributeId);
	}
	
	/**
	 * Gets selector XPath expression
	 * 
	 * @return selector xpath expression
	 */
	public String getPath(){
		return xpath;
	}
	
	/**
	 * Gets context selector attribute identifier
	 * 
	 * @return an attribute identifier or <code>null</code>
	 * if no attribute is specified
	 */
	public String getContextSelectorId(){
		return contextSelectorId;
	}
	
	
	@Override
	public BagOfAttributeValues resolve(EvaluationContext context)
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
}
