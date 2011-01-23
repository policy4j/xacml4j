package com.artagon.xacml.v30;


public class ResponseSyntaxException extends XacmlSyntaxException
{

	private static final long serialVersionUID = 3874931918490495608L;

	public ResponseSyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}

	public ResponseSyntaxException(String message) {
		super(message);
	}

	public ResponseSyntaxException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public ResponseSyntaxException(Throwable cause) {
		super(cause);
	}
	
}
