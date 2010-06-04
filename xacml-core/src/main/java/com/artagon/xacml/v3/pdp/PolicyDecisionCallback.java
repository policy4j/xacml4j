package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.Request;
import com.artagon.xacml.v3.context.Result;

public interface PolicyDecisionCallback 
{
	Result requestDecision(Request request);
}
