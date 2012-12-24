package org.xacml4j.v30.pdp;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;


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
