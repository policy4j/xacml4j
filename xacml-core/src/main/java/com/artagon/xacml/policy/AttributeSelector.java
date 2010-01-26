package com.artagon.xacml.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.CategoryId;
import com.artagon.xacml.util.Preconditions;

public final class AttributeSelector extends AttributeReference
{
	private XPath xpath;
	private boolean mustBePresent;
	
	public AttributeSelector(
			CategoryId category, 
			XPath xpath, 
			AttributeType dataType, 
					boolean mustBePresent){
		super(category, dataType);
		Preconditions.checkNotNull(xpath);
		Preconditions.checkNotNull(dataType);
		this.xpath = xpath;
		this.mustBePresent = mustBePresent;
	}
	
	public boolean isMustBePresent(){
		return mustBePresent;
	}
	
	public XPath getContextPath(){
		return xpath;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributes<?> evaluate(EvaluationContext context)
			throws PolicyEvaluationException {
		BagOfAttributes<?> value = context.resolveAttributeSelector(getCategory(), 
				getContextPath(), evaluatesTo.getDataType());
		if(value.isEmpty() && isMustBePresent()){
			throw new PolicyEvaluationIndeterminateException(
					"Failed to select attribute catgoryId=\"%s\", context path=\"%s\"", 
					getCategory(), xpath.toString());
		}
		return value;
	}	
}
