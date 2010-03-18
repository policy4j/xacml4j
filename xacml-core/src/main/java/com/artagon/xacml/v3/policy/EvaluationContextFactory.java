package com.artagon.xacml.v3.policy;


public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy);
	EvaluationContext createContext(PolicySet policySet);
}
