package com.artagon.xacml.v30.pdp;


import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.dcache.PolicyDecisionCache;
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
}
