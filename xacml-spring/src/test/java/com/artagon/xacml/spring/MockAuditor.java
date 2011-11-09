package com.artagon.xacml.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;

public class MockAuditor implements PolicyDecisionAuditor {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Override
	public void audit(PolicyDecisionPoint pdp, Result result, RequestContext req) {
		log.debug("TestAuditor::audit(result=\"{}\", requestContext=\"{}\"", result, req);
	}
}
