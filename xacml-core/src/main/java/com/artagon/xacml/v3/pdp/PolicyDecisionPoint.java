package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;

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
