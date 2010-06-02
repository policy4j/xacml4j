package com.artagon.xacml.v3;

public class ContextSyntaxException extends XacmlException
{
	private static final long serialVersionUID = -3531176199910284289L;
	
	public ContextSyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}
	
	public ContextSyntaxException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public ContextSyntaxException(Throwable cause) {
		super(cause);
	}
	
	public Status getStatus()
	{
		return new Status(StatusCode.createSyntaxError(), getMessage(), null);
	}
}
