package com.artagon.xacml.v3;

import com.artagon.xacml.v3.policy.impl.DefaultRequest;

public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given XACML {@link DefaultRequest}
	 * and returns {@link Result}
	 * 
	 * @param request a XACML request
	 * @return {@link Result}
	 */
	Response decide(Request request);
}
