package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;


public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given XACML {@link Request}
	 * and returns {@link Result}
	 * 
	 * @param request a XACML request
	 * @return {@link Result}
	 */
	Response decide(Request request);
}
