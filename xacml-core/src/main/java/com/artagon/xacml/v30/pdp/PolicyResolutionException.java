package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.StatusCode;




public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	public PolicyResolutionException(EvaluationContext context,
			String template, Object... arguments) {
		super(StatusCode.createProcessingError(),
				context, template, arguments);
	}

	public PolicyResolutionException(EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(StatusCode.createProcessingError(), 
				context, cause, message, arguments);
	}

	public PolicyResolutionException(EvaluationContext context, 
			Throwable cause) {
		super(StatusCode.createProcessingError(), 
				context, cause);
	}
	
	public PolicyIDReference getPolicyIDReference(){
		return getEvaluationContext().getCurrentPolicyIDReference();
	}
	
	public PolicySetIDReference getPolicySetIDReference(){
		return getEvaluationContext().getCurrentPolicySetIDReference();
	}
}
