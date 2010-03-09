package com.artagon.xacml.v3.policy;


@SuppressWarnings("serial")
public class EvaluationException extends PolicyException
{
	private EvaluationContext context;
	
	protected EvaluationException(EvaluationContext context, 
			String template, Object... arguments) {
		super(template, arguments);
	}

	protected EvaluationException(EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	protected EvaluationException(EvaluationContext context, 
			Throwable cause) {
		super(cause);
	}	
	
	protected EvaluationContext getEvaluationContext(){
		return context;
	}
}
