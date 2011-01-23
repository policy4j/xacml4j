package com.artagon.xacml.v30;

public class XacmlSyntaxException extends XacmlException
{
	private static final long serialVersionUID = 5208193385563540743L;

	public XacmlSyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}
	
	public XacmlSyntaxException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}
	
	public XacmlSyntaxException(Throwable cause) {
		super(cause);
	}
}
