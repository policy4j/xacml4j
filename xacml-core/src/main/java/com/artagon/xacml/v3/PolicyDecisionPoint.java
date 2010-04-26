package com.artagon.xacml.v3;

public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given XACML {@link Request}
	 * and returns {@link Result}
	 * 
	 * @param request a XACML request
	 * @return {@link Result}
	 */
	Response decide(Request request);
}
