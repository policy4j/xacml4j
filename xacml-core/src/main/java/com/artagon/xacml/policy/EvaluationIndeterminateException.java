package com.artagon.xacml.policy;

import com.artagon.xacml.StatusId;

@SuppressWarnings("serial")
public class EvaluationIndeterminateException extends EvaluationException
{
	public EvaluationIndeterminateException(String template, Object... arguments) {
		super(StatusId.STATUS_PROCESSING_ERROR, 
				template, arguments);
	}

	public EvaluationIndeterminateException(Throwable cause, String message,
			Object... arguments) {
		super(StatusId.STATUS_PROCESSING_ERROR,
				cause, message, arguments);
	}	
}