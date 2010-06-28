package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;


class DefaultPolicyInformationPointContext implements PolicyInformationPointContext
{
	private EvaluationContext context;
	
	public DefaultPolicyInformationPointContext(EvaluationContext context){
		Preconditions.checkNotNull(context);
		this.context = context;
	}

	@Override
	public AttributeValue getCurrentDate() {
		return context.getCurrentDate();
	}

	@Override
	public AttributeValue getCurrentDateTime() {
		return context.getCurrentDateTime();
	}

	@Override
	public AttributeValue getCurrentTime() {
		return context.getCurrentTime();
	}
	
	
}
