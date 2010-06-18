package com.artagon.xacml.v3;



public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeReference ref;
	
	public AttributeReferenceEvaluationException(
			EvaluationContext context,
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
		super(StatusCode.createProcessingError(), 
				context, cause);
		this.ref = ref;
	}
	
	public AttributeReference getReference(){
		return ref;
	}
}
