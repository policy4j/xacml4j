package org.xacml4j.v30;

import org.xacml4j.v30.pdp.XacmlException;

import com.google.common.base.Preconditions;


@SuppressWarnings("serial")
public class EvaluationException extends XacmlException
{
	private StatusCode statusCode;
	
	private EvaluationContext context;
	
	public EvaluationException(StatusCode code, 
			EvaluationContext context, 
			String template, Object... arguments) {
		super(template, arguments);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}

	public EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}

	public EvaluationException(StatusCode code, 
			EvaluationContext context, 
			Throwable cause) {
		super(cause);
		Preconditions.checkNotNull(code);
		Preconditions.checkNotNull(context);
		this.statusCode = code;
		this.context = context;
	}	
	
	public final EvaluationContext getEvaluationContext(){
		return context;
	}
	
	public final StatusCode getStatusCode(){
		return statusCode;
	}
}
