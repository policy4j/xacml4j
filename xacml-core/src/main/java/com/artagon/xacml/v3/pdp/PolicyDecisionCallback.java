package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.RequestContextHandler;
import com.artagon.xacml.v3.context.Result;

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
