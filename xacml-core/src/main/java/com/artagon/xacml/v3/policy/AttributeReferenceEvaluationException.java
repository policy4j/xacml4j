package com.artagon.xacml.v3.policy;

public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeReference ref;
	
	public AttributeReferenceEvaluationException(EvaluationContext context,
			AttributeReference ref, 
			String message, Object ...params){
		super(context, message, params);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(EvaluationContext context, 
			AttributeReference ref, 
			Throwable cause){
		super(context, cause);
		this.ref = ref;
	}
	
	public AttributeReference getReference(){
		return ref;
	}
}
