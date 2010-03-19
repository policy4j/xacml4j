package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.XacmlException;


public class PolicyException extends XacmlException
{
	private static final long serialVersionUID = 1529877667754269216L;
	
	protected PolicyException(Throwable cause){
		super(cause);
	}
	
	protected PolicyException(
			String template, Object ... arguments){
		super(template, arguments);
	}
	
	protected PolicyException(Throwable cause, String message, Object ... arguments){
		super(cause, message, arguments);
	}

}
