package com.artagon.xacml.v3;

import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;


public interface EvaluationContextFactory 
{
	EvaluationContext createContext(Policy policy, Request request);
	EvaluationContext createContext(PolicySet policySet, Request request);
}
