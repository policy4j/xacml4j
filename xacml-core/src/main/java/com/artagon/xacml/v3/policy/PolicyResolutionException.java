package com.artagon.xacml.v3.policy;

public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	public PolicyResolutionException(EvaluationContext context,
			String template, Object... arguments) {
		super(context, template, arguments);
	}

	public PolicyResolutionException(EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(context, cause, message, arguments);
	}

	public PolicyResolutionException(EvaluationContext context, 
			Throwable cause) {
		super(context, cause);
	}
}
