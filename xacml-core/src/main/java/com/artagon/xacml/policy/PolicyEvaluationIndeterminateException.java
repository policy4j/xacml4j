package com.artagon.xacml.policy;

import com.artagon.xacml.StatusId;

@SuppressWarnings("serial")
public class PolicyEvaluationIndeterminateException extends PolicyEvaluationException
{
	public PolicyEvaluationIndeterminateException(String template, Object... arguments) {
		super(StatusId.STATUS_PROCESSING_ERROR, 
				template, arguments);
	}

	public PolicyEvaluationIndeterminateException(Throwable cause, String message,
			Object... arguments) {
		super(StatusId.STATUS_PROCESSING_ERROR,
				cause, message, arguments);
	}	
}