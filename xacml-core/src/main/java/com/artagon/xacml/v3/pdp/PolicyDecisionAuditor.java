package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.Result;

public interface PolicyDecisionAuditor 
{
	/**
	 * Generates an audit event from a given
	 * {@link Result} and {@link RequestContext}
	 * 
	 * @param result a decision result
	 * @param req a decision request
	 */
	void audit(Result result, RequestContext req);
}
