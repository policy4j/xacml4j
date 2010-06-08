package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;

public interface PolicyDecisionCallback 
{
	Result requestDecision(Request request);
}
