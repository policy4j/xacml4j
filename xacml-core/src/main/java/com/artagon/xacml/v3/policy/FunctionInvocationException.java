package com.artagon.xacml.v3.policy;

@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	public FunctionInvocationException(String template, Object... arguments) {
		super(template, arguments);
	}

	public FunctionInvocationException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public FunctionInvocationException(Throwable cause) {
		super(cause);
	}
}
