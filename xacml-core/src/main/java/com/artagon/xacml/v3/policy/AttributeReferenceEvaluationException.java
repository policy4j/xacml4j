package com.artagon.xacml.v3.policy;

public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;
	
	private AttributeReference ref;
	
	public AttributeReferenceEvaluationException(AttributeReference ref, 
			String message, Object ...params){
		super(message, params);
		this.ref = ref;
	}
	
	public AttributeReferenceEvaluationException(AttributeReference ref, 
			Throwable cause){
		super(cause);
		this.ref = ref;
	}
	
	public AttributeReference getReference(){
		return ref;
	}
}
