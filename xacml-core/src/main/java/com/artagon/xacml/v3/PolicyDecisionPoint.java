package com.artagon.xacml.v3;

public interface PolicyDecisionPoint 
{
	/**
	 * Evaluates a given XACML request and
	 * returns XACML reponse
	 * 
	 * @param context a request context
	 * @return {@link Response}
	 */
	Response evaluate(Request context);
}
