package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.Result;

public class NoAuditPolicyDecisionPointAuditor implements PolicyDecisionAuditor
{
	@Override
	public void audit(Result result, RequestContext req) {
	}
	
}
