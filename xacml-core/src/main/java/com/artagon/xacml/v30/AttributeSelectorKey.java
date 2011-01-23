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
	
	public String getPath(){
		return xpath;
	}
	
	public String getContextSelectorId(){
		return contextSelectorId;
	}
	
	
	@Override
	public BagOfAttributeValues resolve(EvaluationContext context)
			throws EvaluationException {
		return context.resolve(this);
	}

	public String toString(){
		return Objects.toStringHelper(this)
		.add("Category", getCategory())
		.add("Path", xpath)
		.add("DataType", getDataType())
		.add("ContextSelectorId", contextSelectorId).toString();
	}
	
	public int hashCode(){
		return hashCode;
	}
	
	public boolean equals(Object o)
	{
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
