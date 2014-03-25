package org.xacml4j.v30;

public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	private EvaluationContext context;
	
	public PolicyResolutionException(EvaluationContext context,
			String template, Object... arguments) {
		super(StatusCode.createProcessingError(), template, arguments);
		this.context = context;
	}

	public PolicyResolutionException(EvaluationContext context,
			Throwable cause, String message,
			Object... arguments) {
		super(StatusCode.createProcessingError(), cause, message, arguments);
		this.context = context;
	}

	public PolicyResolutionException(EvaluationContext context,
			Throwable cause) {
		super(StatusCode.createProcessingError(), cause);
		this.context = context;
	}

	public CompositeDecisionRuleIDReference getPolicyIDReference(){
		return context.getCurrentPolicyIDReference();
	}

	public CompositeDecisionRuleIDReference getPolicySetIDReference(){
		return context.getCurrentPolicySetIDReference();
	}
}
