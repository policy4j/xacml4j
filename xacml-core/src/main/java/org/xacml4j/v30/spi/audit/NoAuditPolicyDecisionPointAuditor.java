package org.xacml4j.v30.spi.audit;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;


public class NoAuditPolicyDecisionPointAuditor implements PolicyDecisionAuditor
{
	@Override
	public void audit(PolicyDecisionPoint pdp, Result result, RequestContext req) {
	}

}
