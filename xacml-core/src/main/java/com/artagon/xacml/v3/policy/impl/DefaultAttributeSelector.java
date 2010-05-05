package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.PolicyVisitor;


final class DefaultAttributeSelector extends 
	BaseAttributeReference implements AttributeSelector
{
	private String xpath;
	private String contextAttributeId;
	
	public DefaultAttributeSelector(
			AttributeCategoryId category, 
			String xpath, 
			AttributeValueType dataType, 
					boolean mustBePresent){
		super(category, dataType, mustBePresent);
		Preconditions.checkNotNull(xpath);
		this.xpath = xpath;
	}
	
	@Override
	public String getSelect(){
		return xpath;
	}
	
	@Override
	public String getContextAttributeId()
	{
		return contextAttributeId;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeValues<?> evaluate(EvaluationContext context)
			throws EvaluationException 
	{ 
		return context.resolve(this);
	}
	
	
}
