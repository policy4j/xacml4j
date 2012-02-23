package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.StatusCode;




@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;
	
	public FunctionInvocationException(EvaluationContext context,
			FunctionSpec spec, 
			String template, Object... arguments) {
		super(StatusCode.createProcessingError(),
				context, template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			EvaluationContext context,
			FunctionSpec spec, 
			Throwable cause, String message,
			Object... arguments) {
		super(StatusCode.createProcessingError(), 
				context, cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(EvaluationContext context, 
			FunctionSpec spec, Throwable cause) {
		super(StatusCode.createProcessingError(), 
				context, cause);
		this.spec = spec;
	}
	
	public FunctionSpec getSpec(){
		return spec;
	}
}
