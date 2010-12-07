package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.ResponseContext;
import com.artagon.xacml.v3.context.Result;

public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given XACML {@link RequestContext}
	 * and returns {@link Result}
	 * 
	 * @param request a XACML request
	 * @return {@link Result}
	 */
	ResponseContext decide(RequestContext request);
}
