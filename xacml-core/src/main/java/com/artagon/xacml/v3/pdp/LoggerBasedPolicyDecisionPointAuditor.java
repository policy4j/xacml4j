package com.artagon.xacml.v3.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public class LoggerBasedPolicyDecisionPointAuditor implements PolicyDecisionPointAuditor
{
	private final static Logger log = LoggerFactory.getLogger(LoggerBasedPolicyDecisionPointAuditor.class);
	
	@Override
	public void audit(Result result, RequestContext req) {
		if(log.isInfoEnabled()){
			log.info("Audit event: request=\"{}\" decision result=\"{}\"", req, result);
		}
	}
	
}
