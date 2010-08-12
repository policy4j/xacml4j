package com.artagon.xacml.v3.pdp;

import java.util.Collection;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public interface RequestProfileHandler 
{
	/**
	 * Handles given request
	 * 
	 * @param request a decision request
	 * @param pdp a policy decision point callback
	 * @return collection of {@link Result} instances
	 */
	Collection<Result> handle(RequestContext request, 
			PolicyDecisionCallback pdp);
	
	/**
	 * Sets next handler in a chain
	 * 
	 * @param handler a request handler
	 */
	void setNext(RequestProfileHandler handler);
}