package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;

/**
 * An interface to the {@link PolicyDecisionPoint} used
 * by {@link RequestContextHandler} to request decision
 * for particular request
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyDecisionCallback 
{
	/**
	 * Requests a decision for a given request
	 * from a policy decision point
	 * 
	 * @param request a decision request
	 * @return {@link Result} a decision result
	 */
	Result requestDecision(PolicyDecisionPointContext context, 
			RequestContext request);
}
