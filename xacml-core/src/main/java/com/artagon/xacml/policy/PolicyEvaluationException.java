package com.artagon.xacml.policy;

import com.artagon.xacml.StatusId;

@SuppressWarnings("serial")
public class PolicyEvaluationException extends Exception
{
	/**
	 * Constructs exception with a given status and message.
	 * 
	 * @param status an evaluation status
	 * @param template a template {@see String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected PolicyEvaluationException(StatusId statusId,
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
	protected PolicyEvaluationException(StatusId status,
			Throwable cause, String message, Object ... arguments){
		super(String.format(message, arguments), cause);
	}	
}
