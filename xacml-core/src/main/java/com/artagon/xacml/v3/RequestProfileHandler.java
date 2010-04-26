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
		
	Collection<Result> handle(Request request, PolicyDecisionCallback pdp);
	
	void setNext(RequestProfileHandler handler);
}