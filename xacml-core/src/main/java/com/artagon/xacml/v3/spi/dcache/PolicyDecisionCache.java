package com.artagon.xacml.v3.spi.dcache;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

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
	 * request to this cache
	 * 
	 * @param req a decision request
	 * @param result a decision result for a given request
	 */
	void putDecision(RequestContext req, Result result);
}
