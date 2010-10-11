package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import com.artagon.xacml.v3.AttributeReference;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.google.common.base.Preconditions;

class DefaultPolicyInformationPointContext 
	implements PolicyInformationPointContext
{
	private AttributeReference ref;
	private EvaluationContext context;
	private RequestContextCallback callback;
	
	public DefaultPolicyInformationPointContext(
			EvaluationContext context, 
			RequestContextCallback callback,
			AttributeReference ref){
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(callback);
		Preconditions.checkNotNull(ref);
		this.context = context;
		this.ref = ref;
		this.callback = callback;
	}
	
	@Override
	public Calendar getCurrentDateTime() {
		return context.getCurrentDateTime();
	}
	
	@Override
	public Object getValue(Object key) {
		return context.getValue(ref.getCategory(), key);
	}

	@Override
	public Object setValue(Object key, Object v) {
		return context.setValue(ref.getCategory(), key, v);
	}	
	
	@Override
	public RequestContextCallback getRequestContextCallback(){
		return callback;
	}
}
