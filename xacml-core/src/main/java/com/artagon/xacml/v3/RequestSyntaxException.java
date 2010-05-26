package com.artagon.xacml.v3;

public class RequestSyntaxException extends XacmlException
{
	private static final long serialVersionUID = -3531176199910284289L;
	
	public RequestSyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}
	
	public RequestSyntaxException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public RequestSyntaxException(Throwable cause) {
		super(cause);
	}
	
	public Status getStatus(){
		return new Status(StatusCode.createSyntaxError(), getMessage(), null);
	}
}
