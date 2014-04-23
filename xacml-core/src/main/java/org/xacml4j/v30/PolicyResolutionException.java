package org.xacml4j.v30;

public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	private EvaluationContext context;
	
	public PolicyResolutionException(EvaluationContext context,
			String template, Object... arguments) {
		super(Status.processingError().build(), template, arguments);
		this.context = context;
	}

	public PolicyResolutionException(EvaluationContext context,
			Throwable cause, String message,
			Object... arguments) {
		super(Status.processingError().build(), cause, message, arguments);
		this.context = context;
	}

	public PolicyResolutionException(EvaluationContext context,
			Throwable cause) {
		super(Status.processingError().build(), cause);
		this.context = context;
	}

	public CompositeDecisionRuleIDReference getPolicyIDReference(){
		return context.getCurrentPolicyIDReference();
	}

	public CompositeDecisionRuleIDReference getPolicySetIDReference(){
		return context.getCurrentPolicySetIDReference();
	}
}
