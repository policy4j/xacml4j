package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.StatusCode;


@SuppressWarnings("serial")
public class EvaluationException extends PolicyException
{
	private StatusCode statusCode;
	
	private EvaluationContext context;
	
	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			String template, Object... arguments) {
		super(template, arguments);
		this.statusCode = code;
	}

	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		this.statusCode = code;
	}

	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause) {
		super(cause);
		this.statusCode = code;
	}	
	
	public final EvaluationContext getEvaluationContext(){
		return context;
	}
	
	public final StatusCode getStatusCode(){
		return statusCode;
	}
}
