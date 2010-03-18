package com.artagon.xacml.v3;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.PolicyException;


@SuppressWarnings("serial")
public class EvaluationException extends PolicyException
{
	private StatusCode statusCode;
	
	private EvaluationContext context;
	
	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			String template, Object... arguments) {
		super(template, arguments);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}

	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}

	protected EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause) {
		super(cause);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}	
	
	public final EvaluationContext getEvaluationContext(){
		return context;
	}
	
	public final StatusCode getStatusCode(){
		return statusCode;
	}
}
