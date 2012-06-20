package com.artagon.xacml.v30.spi.pdp;

import java.util.Collection;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.Result;

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
	 * Gets list of supported features by this handler
	 * 
	 * @return a list of supported feature identifiers
	 */
	Collection<String> getFeatures();
	
	/**
	 * Sets next handler in a chain
	 * 
	 * @param handler a next in chain request handler
	 * @exception IllegalStateException if "next" handler 
	 * is already set for this handler
	 */
	void setNext(RequestContextHandler handler);
}