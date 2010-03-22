package com.artagon.xacml.v3;


public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates given request
	 * 
	 * @param context a request context
	 * @return {@link ResponseContext}
	 */
	ResponseContext evaluate(RequestContext context);
}
