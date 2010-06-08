package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.StatusCode;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeReference ref;
	
	public AttributeReferenceEvaluationException(EvaluationContext context,
			AttributeReference ref, 
			String message, Object ...params){
		super(StatusCode.createMissingAttribute(),
				context, message, params);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(
			StatusCode statusCode,
			EvaluationContext context,
			AttributeReference ref, 
			String message, Object ...params){
		super(statusCode, context, message, params);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(EvaluationContext context, 
			AttributeReference ref, 
			Throwable cause){
		super(StatusCode.createMissingAttribute(), 
				context, cause);
		this.ref = ref;
	}
	
	public AttributeReference getReference(){
		return ref;
	}
}
