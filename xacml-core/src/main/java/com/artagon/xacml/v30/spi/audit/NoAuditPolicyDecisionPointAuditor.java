package com.artagon.xacml.v30.spi.audit;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;

public class NoAuditPolicyDecisionPointAuditor implements PolicyDecisionAuditor
{
	@Override
	public void audit(Result result, RequestContext req) {
	}
	
}
