package org.xacml4j.v30;

import javax.xml.stream.Location;

import org.xacml4j.v30.pdp.XacmlException;


public class XacmlSyntaxException extends XacmlException
{
	private static final long serialVersionUID = 5208193385563540743L;

	public XacmlSyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}

	public XacmlSyntaxException(
			Throwable t,
			String message,
			Object... arguments) {
		super(t, message, arguments);
	}

	public XacmlSyntaxException(
			Location location,
			String message,
			Object... arguments) {
		super(String.format("XACML syntax error at line=\"%s\" column=\"%s\", error: %s",
				location.getLineNumber(),
				location.getColumnNumber(),
				String.format(message, arguments)));
	}

	public XacmlSyntaxException(Throwable cause) {
		super(cause);
	}
}
