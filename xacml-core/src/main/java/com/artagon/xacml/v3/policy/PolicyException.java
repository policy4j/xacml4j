package com.artagon.xacml.v3.policy;


public class PolicyException extends Exception
{
	private static final long serialVersionUID = 1529877667754269216L;
	
	protected PolicyException(Throwable cause){
		super(cause);
	}
	
	/**
	 * Constructs exception with a given message.
	 * 
	 * @param template a template {@see String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected PolicyException(
			String template, Object ... arguments){
		super(String.format(template, arguments));
	}
	
	/**
	 * Constructs exception with a given status and message.
	 * 
	 * @param cause a root cause of this exception
	 * @param template a template {@see String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected PolicyException(Throwable cause, String message, Object ... arguments){
		super(String.format(message, arguments), cause);
	}

}
