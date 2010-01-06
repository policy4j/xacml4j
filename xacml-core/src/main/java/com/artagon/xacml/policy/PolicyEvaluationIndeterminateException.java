package com.artagon.xacml.policy;

import com.artagon.xacml.Statuses;

@SuppressWarnings("serial")
public class PolicyEvaluationIndeterminateException extends PolicyEvaluationException
{
	public PolicyEvaluationIndeterminateException(String template, Object... arguments) {
		super(Statuses.STATUS_PROCESSING_ERROR, 
				template, arguments);
	}

	public PolicyEvaluationIndeterminateException(Throwable cause, String message,
			Object... arguments) {
		super(Statuses.STATUS_PROCESSING_ERROR,
				cause, message, arguments);
	}	
}