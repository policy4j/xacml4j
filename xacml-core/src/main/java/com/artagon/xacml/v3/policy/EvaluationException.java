package com.artagon.xacml.v3.policy;


@SuppressWarnings("serial")
public class EvaluationException extends PolicyException
{
	private EvaluationContext context;
	
	public EvaluationException(String template, Object... arguments) {
		super(template, arguments);
	}

	public EvaluationException(Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
	}

	public EvaluationException(Throwable cause) {
		super(cause);
	}	
	
	public EvaluationContext getEvaluationContext(){
		return context;
	}
	
	public void setEvaluationContext(EvaluationContext context){
		this.context = context;
	}
}
