package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.Result;

public interface PolicyDecisionCache 
{
	/**
	 * Gets a cached instance of {@link Result}
	 * from this cache
	 * 
	 * @param req a decision request
	 * @return {@link Result}
	 */
	Result getDecision(RequestContext req);
	
	/**
	 * Puts a decision result {@link Result} for a given
	 * request to the cache
	 * 
	 * @param req a request
	 * @param result a decision result
	 */
	void putDecision(RequestContext req, Result result);
}
