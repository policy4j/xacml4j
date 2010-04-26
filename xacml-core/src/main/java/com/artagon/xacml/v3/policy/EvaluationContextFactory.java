package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Request;


public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy, Request request);
	EvaluationContext createContext(PolicySet policySet, Request request);
}
