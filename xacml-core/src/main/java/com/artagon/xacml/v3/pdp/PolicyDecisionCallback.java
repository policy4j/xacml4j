package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.spi.XPathProvider;

/**
 * An interface to the {@link PolicyDecisionPoint} used
 * by {@link RequestContextHandler} to request decision
 * for particular request
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyDecisionCallback 
{
	/**
	 * Requests a decision for a given request
	 * from a policy decision point
	 * 
	 * @param request a decision request
	 * @return {@link Result} a decision result
	 */
	Result requestDecision(RequestContext request);
	
	/**
	 * Gets an {@link XPathProvider} an XPath provider
	 * 
	 * @return {@link XPathProvider}
	 */
	XPathProvider getXPathProvider();
}
