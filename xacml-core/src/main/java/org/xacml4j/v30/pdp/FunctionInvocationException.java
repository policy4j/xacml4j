package org.xacml4j.v30.pdp;

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;


@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;

	public FunctionInvocationException(
			FunctionSpec spec,
			String template, Object... arguments) {
		super(Status.processingError().build(), template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			FunctionSpec spec,
			Throwable cause, String message,
			Object... arguments) {
		super(Status.processingError().build(),
				cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(FunctionSpec spec, Throwable cause) {
		super(Status.processingError().build(), cause);
		this.spec = spec;
	}

	public FunctionSpec getSpec(){
		return spec;
	}
}
