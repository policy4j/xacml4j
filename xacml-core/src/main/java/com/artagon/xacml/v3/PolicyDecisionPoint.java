package com.artagon.xacml.v3;

public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given request and
	 * return a policy decision result
	 * 
	 * @param context a request context
	 * @return {@link Result} a decision
	 * result
	 */
	Result evaluate(Request context);
}
