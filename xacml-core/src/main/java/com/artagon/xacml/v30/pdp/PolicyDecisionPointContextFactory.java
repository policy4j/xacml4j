package com.artagon.xacml.v30.pdp;

public interface PolicyDecisionPointContextFactory 
{
	/**
	 * Creates policy decision context
	 * 
	 * @param pdp a PDP callback
	 * @return {@link PolicyDecisionPointContext} instance
	 */
	PolicyDecisionPointContext createContext(PolicyDecisionCallback pdp);
}
