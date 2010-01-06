package com.artagon.xacml.policy;

public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy);
	EvaluationContext createContext(PolicySet policySet);
}
