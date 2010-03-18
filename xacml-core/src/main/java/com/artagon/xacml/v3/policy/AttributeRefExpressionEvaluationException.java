package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.StatusCode;

public class AttributeRefExpressionEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeRefExpression ref;
	
	public AttributeRefExpressionEvaluationException(EvaluationContext context,
			AttributeRefExpression ref, 
			String message, Object ...params){
		super(StatusCode.createMissingAttribute(),
				context, message, params);
		this.ref = ref;
	}
	
	public AttributeRefExpressionEvaluationException(
			StatusCode statusCode,
			EvaluationContext context,
			AttributeRefExpression ref, 
			String message, Object ...params){
		super(statusCode, context, message, params);
		this.ref = ref;
	}
	
	public AttributeRefExpressionEvaluationException(EvaluationContext context, 
			AttributeRefExpression ref, 
			Throwable cause){
		super(StatusCode.createMissingAttribute(), 
				context, cause);
		this.ref = ref;
	}
	
	public AttributeRefExpression getReference(){
		return ref;
	}
}
