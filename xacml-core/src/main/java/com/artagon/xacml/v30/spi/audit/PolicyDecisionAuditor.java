package com.artagon.xacml.v30.spi.audit;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;

public interface PolicyDecisionAuditor 
{
	/**
	 * Generates an audit event from a given
	 * {@link Result} and {@link RequestContext}
	 * 
	 * @param result a decision result
	 * @param req a decision request
	 */
	void audit(PolicyDecisionPoint pdp, Result result, RequestContext req);
}
