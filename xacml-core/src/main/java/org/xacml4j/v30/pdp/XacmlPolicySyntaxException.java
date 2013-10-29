package org.xacml4j.v30.pdp;

import javax.xml.stream.Location;

import org.xacml4j.v30.XacmlSyntaxException;


public class XacmlPolicySyntaxException extends XacmlSyntaxException
{
	private static final long serialVersionUID = 345591401946746019L;

	public XacmlPolicySyntaxException(
			String policyId,
			Location location,
			String errorMessage,
			Object... errorMessageArgs) {
		super(String.format(
				"Policy=\"%s\" syntax error at " +
				"line=\"%s\" column=\"%s\", error: %s",
				policyId,
				location.getLineNumber(),
				location.getColumnNumber(),
				String.format(errorMessage,
						errorMessageArgs)));
	}
}
