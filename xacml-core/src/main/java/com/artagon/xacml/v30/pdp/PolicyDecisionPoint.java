package com.artagon.xacml.v30.pdp;


public interface PolicyDecisionPoint extends PolicyDecisionPointMBean
{
	/**
	 * Gets policy decision point
	 * unique identifier
	 * 
	 * @return unique identifier
	 */
	String getId();
	
	/**
	 * Evaluates a given XACML {@link RequestContext}
	 * and returns {@link Result}
	 * 
	 * @param request a XACML request
	 * @return {@link Result}
	 */
	ResponseContext decide(RequestContext request);
}
