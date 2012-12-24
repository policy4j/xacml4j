package org.xacml4j.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;


public class MockAuditor implements PolicyDecisionAuditor {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Override
	public void audit(PolicyDecisionPoint pdp, Result result, RequestContext req) {
		log.debug("TestAuditor::audit(result=\"{}\", requestContext=\"{}\"", result, req);
	}
}
