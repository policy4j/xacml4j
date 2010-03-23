package com.artagon.xacml.v3;


public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates given request
	 * 
	 * @param context a request context
	 * @return {@link Response}
	 */
	Result evaluate(Request context);
}
