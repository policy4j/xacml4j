package com.artagon.xacml.policy;

import com.artagon.xacml.StatusId;
import com.artagon.xacml.Statuses;

public class PolicySyntaxException extends PolicyException
{

	private static final long serialVersionUID = 5110785800396453262L;

	public PolicySyntaxException(String template,
			Object... arguments) {
		super(Statuses.SYNTAX_ERROR, template, arguments);
	}

	public PolicySyntaxException(StatusId status, Throwable cause,
			String message, Object... arguments) {
		super(Statuses.SYNTAX_ERROR, cause, message, arguments);
	}
}
