package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandler;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;

public interface PolicyDecisionPointContext
{
	/**
	 * Creates {@link EvaluationContext} to evaluate
	 * given {@link RequestContext} access decision request
	 * 
	 * @param req an access decision request
	 * @return {@link EvaluationContext}
	 */
	EvaluationContext createEvaluationContext(RequestContext req);
	
	CompositeDecisionRule getDomainPolicy();
	
	XPathProvider getXPathProvider();
	
	PolicyDecisionCache getDecisionCache();
	
	PolicyDecisionAuditor getDecisionAuditor();
	
	Result requestDecision(RequestContext req);
	
	RequestContextHandler getRequestHandlers();
	
	boolean isDecisionCacheEnabled();
	boolean isDecisionAuditEnabled();
	boolean isValidateFuncParamsAtRuntime();
}
