package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.google.common.base.Preconditions;

class DefaultPolicyInformationPointContext 
	implements PolicyInformationPointContext
{
	private AttributeCategory category;
	private EvaluationContext context;
	private RequestContextCallback callback;
	
	public DefaultPolicyInformationPointContext(
			EvaluationContext context, 
			RequestContextCallback callback,
			AttributeCategory category){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(callback);
		Preconditions.checkNotNull(category);
		this.context = context;
		this.category = category;
		this.callback = callback;
	}
	
	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}
	
	@Override
	public Object getValue(Object key) {
		return context.getValue(category, key);
	}

	@Override
	public Object setValue(Object key, Object v) {
		return context.setValue(category, key, v);
	}	
	
	@Override
	public RequestContextCallback getRequestContextCallback(){
		return callback;
	}
}
