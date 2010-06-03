package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;

public interface PolicyDecisionPoint 
{
	Response decide(Request request);
}
