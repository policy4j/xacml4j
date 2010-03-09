package com.artagon.xacml.v3.policy;

@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;
	
	public FunctionInvocationException(FunctionSpec spec, 
			String template, Object... arguments) {
		super(template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			FunctionSpec spec, 
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(FunctionSpec spec, Throwable cause) {
		super(cause);
		this.spec = spec;
	}
	
	public FunctionSpec getSpec(){
		return spec;
	}
}
