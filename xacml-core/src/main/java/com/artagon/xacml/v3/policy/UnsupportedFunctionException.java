package com.artagon.xacml.v3.policy;

public class UnsupportedFunctionException extends PolicySyntaxException
{
	private static final long serialVersionUID = 7013114924377190708L;

	public UnsupportedFunctionException(String functionId) {
		super(functionId);
	}	
}
