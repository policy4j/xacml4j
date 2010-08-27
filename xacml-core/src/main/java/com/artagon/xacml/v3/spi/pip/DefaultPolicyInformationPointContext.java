package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;

class DefaultPolicyInformationPointContext implements PolicyInformationPointContext
{
	private AttributeReference ref;
	private EvaluationContext context;
	
	public DefaultPolicyInformationPointContext(EvaluationContext context, 
			AttributeReference ref){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(ref);
		this.context = context;
		this.ref = ref;
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

	@Override
	public Object getValue(Object key) {
		return context.getValue(ref.getCategory(), key);
	}

	@Override
	public Object setValue(Object key, Object v) {
		return context.setValue(ref.getCategory(), key, v);
	}	
}
