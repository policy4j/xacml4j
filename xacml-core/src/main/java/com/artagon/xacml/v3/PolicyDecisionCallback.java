package com.artagon.xacml.v3;

public interface PolicyDecisionCallback 
{
	Result requestDecision(Request request);
}
