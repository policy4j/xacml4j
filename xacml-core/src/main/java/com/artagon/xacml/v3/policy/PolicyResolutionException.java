package com.artagon.xacml.v3.policy;

public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	public PolicyResolutionException(String template, Object... arguments) {
		super(template, arguments);
	}

	public PolicyResolutionException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public PolicyResolutionException(Throwable cause) {
		super(cause);
	}
}
