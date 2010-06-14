package com.artagon.xacml.v3;



public interface EvaluationContextFactory 
{
	EvaluationContext createContext(CompositeDecisionRule policy, Request request);
}
