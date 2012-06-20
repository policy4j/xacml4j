package com.artagon.xacml.v30.spi.pdp;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;

public interface PolicyDecisionCache extends PolicyDecisionCacheMBean
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
	 * request to this cache
	 * 
	 * @param req a decision request
	 * @param result a decision result for a given request
	 * @param ttl a time in seconds
	 */
	void putDecision(RequestContext req, Result result, int ttl);
}
