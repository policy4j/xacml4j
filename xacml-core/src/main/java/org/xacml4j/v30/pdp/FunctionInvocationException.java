package org.xacml4j.v30.pdp;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCode;




@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;

	public FunctionInvocationException(EvaluationContext context,
			FunctionSpec spec,
			String template, Object... arguments) {
		super(StatusCode.createProcessingError(), template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			EvaluationContext context,
			FunctionSpec spec,
			Throwable cause, String message,
			Object... arguments) {
		super(StatusCode.createProcessingError(),
				cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(EvaluationContext context,
			FunctionSpec spec, Throwable cause) {
		super(StatusCode.createProcessingError(), cause);
		this.spec = spec;
	}

	public FunctionSpec getSpec(){
		return spec;
	}
}
