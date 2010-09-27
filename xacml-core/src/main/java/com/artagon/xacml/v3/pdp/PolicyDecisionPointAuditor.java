package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public interface PolicyDecisionPointAuditor 
{
	void audit(Result result, RequestContext req);
}
