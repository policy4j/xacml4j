package com.artagon.xacml.v3.context;

import java.util.Collection;

import com.artagon.xacml.v3.pdp.PolicyDecisionPointContext;

public interface RequestContextHandler 
{
	/**
	 * Handles given request
	 * 
	 * @param request a decision request
	 * @param pdp a policy decision point callback
	 * @return collection of {@link Result} instances
	 */
	Collection<Result> handle(
			RequestContext request, 
			PolicyDecisionPointContext context);
	
	/**
	 * Sets next handler in a chain
	 * 
	 * @param handler a next in chain request handler
	 * @exception IllegalStateException if "next" handler 
	 * is already set for this handler
	 */
	void setNext(RequestContextHandler handler);
}