package com.artagon.xacml.v3;

import com.artagon.xacml.util.Preconditions;

public class RequestContextException extends XacmlException
{
	private static final long serialVersionUID = -8700243289139962516L;
	private StatusCode statusCode;
	
	public RequestContextException(StatusCode code, 
			String template, Object... arguments) {
		super(template, arguments);
		Preconditions.checkNotNull(code);
		this.statusCode = code;
	}

	public RequestContextException(StatusCode code, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		Preconditions.checkNotNull(code);
		this.statusCode = code;
	}

	public RequestContextException(StatusCode code, 
			Throwable cause) {
		super(cause);
		Preconditions.checkNotNull(code);
		this.statusCode = code;
	}	
	
	public final StatusCode getStatusCode(){
		return statusCode;
	}
}
