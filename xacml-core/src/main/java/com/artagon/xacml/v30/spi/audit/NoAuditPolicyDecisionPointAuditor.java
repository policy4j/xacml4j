package com.artagon.xacml.v30.spi.audit;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;

public class NoAuditPolicyDecisionPointAuditor implements PolicyDecisionAuditor
{
	@Override
	public void audit(PolicyDecisionPoint pdp, Result result, RequestContext req) {
	}
	
}
