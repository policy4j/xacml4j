package com.artagon.xacml.v30.spi.audit;

import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.Result;

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
