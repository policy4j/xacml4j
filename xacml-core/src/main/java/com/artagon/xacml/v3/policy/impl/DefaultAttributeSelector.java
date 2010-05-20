package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.google.common.base.Preconditions;


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
		BagOfAttributeValues<AttributeValue> bag =  context.resolve(this);
		if((bag == null || bag.isEmpty()) 
				&& isMustBePresent()){
			throw new AttributeReferenceEvaluationException(context, this, 
				"Selector XPath expression=\"%s\" evaluated " +
				"to empty node set and mustBePresents=\"true\"", getSelect());
		}
		return (bag == null)?getDataType().bagOf().createEmpty():bag;
	}
	
	
}
