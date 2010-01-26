package com.artagon.xacml.v30.policy;

public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy);
	EvaluationContext createContext(PolicySet policySet);
}
