package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.StatusCode;
import com.google.common.base.Preconditions;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeReferenceKey ref;
	
	public AttributeReferenceEvaluationException(
			EvaluationContext context,
			AttributeReferenceKey ref, 
			String message, Object ...params){
		super(StatusCode.createMissingAttribute(),
				context, message, params);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(
			StatusCode statusCode,
			EvaluationContext context,
			AttributeReferenceKey ref, 
			String message, Object ...params){
		super(statusCode, context, message, params);
		Preconditions.checkNotNull(ref);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(
			EvaluationContext context, 
			AttributeReferenceKey ref,
			StatusCode code,
			Throwable cause){
		super(code, 
				context, cause);
		this.ref = ref;
	}
	
	public AttributeReferenceKey getReference(){
		return ref;
	}
}
