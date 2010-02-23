package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;

final class DefaultAttributeSelector extends 
	BaseAttributeReference implements AttributeSelector
{
	private XPath xpath;
	
	public DefaultAttributeSelector(
			AttributeCategoryId category, 
			XPath xpath, 
			AttributeValueType dataType, 
					boolean mustBePresent){
		super(category, dataType, mustBePresent);
		Preconditions.checkNotNull(xpath);
		Preconditions.checkNotNull(dataType);
		this.xpath = xpath;
	}
	
	@Override
	public XPath getContextPath(){
		return xpath;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeValues<?> evaluate(EvaluationContext context)
			throws EvaluationException {
		BagOfAttributeValues<?> value = context.resolveAttributeSelector(getCategory(), 
				getContextPath(), evaluatesTo.getDataType());
		if(value.isEmpty() && isMustBePresent()){
			throw new EvaluationException(
					"Failed to select attribute catgoryId=\"%s\", context path=\"%s\"", 
					getCategory(), xpath.toString());
		}
		return value;
	}	
}
