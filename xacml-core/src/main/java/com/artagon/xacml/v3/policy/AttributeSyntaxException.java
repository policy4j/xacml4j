package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.StatusId;

@SuppressWarnings("serial")
public class AttributeSyntaxException extends PolicyException
{
	public AttributeSyntaxException(String template,
			Object... arguments) {
		super(StatusId.SYNTAX_ERROR, template, arguments);
	}
	
	public AttributeSyntaxException(Throwable cause,
			String message, Object... arguments) {
		super(StatusId.SYNTAX_ERROR, cause, message, arguments);
	}	
}
