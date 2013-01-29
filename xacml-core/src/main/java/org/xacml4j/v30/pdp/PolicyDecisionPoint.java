package org.xacml4j.v30.pdp;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;


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

	void close();
}
