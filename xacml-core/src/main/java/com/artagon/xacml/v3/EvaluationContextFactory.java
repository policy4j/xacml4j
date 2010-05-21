package com.artagon.xacml.v3;


public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy, Request request);
	EvaluationContext createContext(PolicySet policySet, Request request);
}
