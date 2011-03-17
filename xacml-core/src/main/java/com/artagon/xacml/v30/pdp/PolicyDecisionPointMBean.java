package com.artagon.xacml.v30.pdp;

public interface PolicyDecisionPointMBean 
{
	long getDecisionRequestCount();
	void resetCounts();
}
