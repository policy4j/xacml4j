package com.artagon.xacml.v3.policy;

public class PolicySyntaxException extends PolicyException
{
	private static final long serialVersionUID = 5208193385563540743L;

	protected PolicySyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}
	
	protected PolicySyntaxException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}
	
	protected PolicySyntaxException(Throwable cause) {
		super(cause);
	}
}
