package com.artagon.xacml.v3.policy;

@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;
	
	public FunctionInvocationException(EvaluationContext context,
			FunctionSpec spec, 
			String template, Object... arguments) {
		super(context, template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			EvaluationContext context,
			FunctionSpec spec, 
			Throwable cause, String message,
			Object... arguments) {
		super(context, cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(EvaluationContext context, 
			FunctionSpec spec, Throwable cause) {
		super(context, cause);
		this.spec = spec;
	}
	
	public FunctionSpec getSpec(){
		return spec;
	}
}
