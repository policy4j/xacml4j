package com.artagon.xacml.v3.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public class NoAuditPolicyDecisionPointAuditor implements PolicyDecisionAuditor
{
	private final static Logger log = LoggerFactory.getLogger(NoAuditPolicyDecisionPointAuditor.class);
	
	@Override
	public void audit(Result result, RequestContext req) {
	}
	
}
