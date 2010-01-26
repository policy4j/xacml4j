package com.artagon.xacml.v30.policy;

import com.artagon.xacml.v30.StatusId;

public class PolicyException extends Exception
{
	private static final long serialVersionUID = 1529877667754269216L;
	
	/**
	 * Constructs exception with a given status and message.
	 * 
	 * @param status an evaluation status
	 * @param template a template {@see String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected PolicyException(StatusId statusId,
			String template, Object ... arguments){
		super(String.format(template, arguments));
	}
	
	/**
	 * Constructs exception with a given status and message.
	 * 
	 * @param status an evaluation status
	 * @param cause a root cause of this exception
	 * @param template a template {@see String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected PolicyException(StatusId status,
			Throwable cause, String message, Object ... arguments){
		super(String.format(message, arguments), cause);
	}

}
