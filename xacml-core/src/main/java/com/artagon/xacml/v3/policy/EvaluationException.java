package com.artagon.xacml.v3.policy;


@SuppressWarnings("serial")
public class EvaluationException extends PolicyException
{

	protected EvaluationException(String template, Object... arguments) {
		super(template, arguments);
	}

	protected EvaluationException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	protected EvaluationException(Throwable cause) {
		super(cause);
	}	
}
