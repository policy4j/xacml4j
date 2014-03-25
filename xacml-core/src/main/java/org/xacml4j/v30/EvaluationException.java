package org.xacml4j.v30;

import org.xacml4j.v30.pdp.XacmlException;

import com.google.common.base.Preconditions;


@SuppressWarnings("serial")
public class EvaluationException extends XacmlException
{
	private StatusCode statusCode;

	public EvaluationException(StatusCode code,
			String template, Object... arguments) {
		super(template, arguments);
		Preconditions.checkNotNull(code);
		this.statusCode = code;
	}

	public EvaluationException(StatusCode code,
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		Preconditions.checkNotNull(code);
		this.statusCode = code;
	}

	public EvaluationException(StatusCode code,
			Throwable cause) {
		super(cause);
		Preconditions.checkNotNull(code);

		this.statusCode = code;
	}


	public final StatusCode getStatusCode(){
		return statusCode;
	}
}
