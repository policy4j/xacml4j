package org.xacml4j.v30;

import org.xacml4j.v30.pdp.XacmlException;

import com.google.common.base.Preconditions;


@SuppressWarnings("serial")
public class EvaluationException extends XacmlException
{
	private Status status;

	public EvaluationException(Status code,
			String template, Object... arguments) {
		super(template, arguments);
		Preconditions.checkNotNull(code);
		this.status = code;
	}

	public EvaluationException(Status code,
			Throwable cause, String message,
			Object... arguments) {
		super(cause, message, arguments);
		Preconditions.checkNotNull(code);
		this.status = code;
	}

	public EvaluationException(Status code,
			Throwable cause) {
		super(cause);
		Preconditions.checkNotNull(code);

		this.status = code;
	}


	public final Status getStatus(){
		return status;
	}
}
