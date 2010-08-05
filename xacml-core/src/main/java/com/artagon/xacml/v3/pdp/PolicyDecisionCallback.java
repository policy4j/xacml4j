package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

/**
 * An interface to the {@link PolicyDecisionPoint} used
 * by {@link RequestProfileHandler} to request decision
 * for particular request
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyDecisionCallback 
{
	Result requestDecision(RequestContext request);
}
