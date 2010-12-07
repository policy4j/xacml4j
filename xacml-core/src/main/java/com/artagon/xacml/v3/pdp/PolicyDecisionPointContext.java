package com.artagon.xacml.v3.pdp;


import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.context.Result;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.spi.XPathProvider;

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
