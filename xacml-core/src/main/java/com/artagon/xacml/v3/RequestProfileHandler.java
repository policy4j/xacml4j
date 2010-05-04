package com.artagon.xacml.v3;

import java.util.Collection;

public interface RequestProfileHandler 
{
	/**
	 * Gets profile identifier
	 * 
	 * @return 
	 */
	String getId();
	
	/**
	 * Handles given request
	 * 
	 * @param request a decision request
	 * @param pdp a policy decision point
	 * @return collection of {@link Result} instances
	 */
	Collection<Result> handle(Request request, PolicyDecisionCallback pdp);
	
	/**
	 * Sets next handler in a chain
	 * 
	 * @param handler a request handler
	 */
	void setNext(RequestProfileHandler handler);
}