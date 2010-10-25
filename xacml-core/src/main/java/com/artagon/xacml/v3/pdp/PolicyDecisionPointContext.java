package com.artagon.xacml.v3.pdp;


import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;

public interface PolicyDecisionPointContext
{
		
	EvaluationContext createEvaluationContext(RequestContext req);
	
	PolicyDomain getPolicyDomain();
	
	XPathProvider getXPathProvider();
	
	PolicyDecisionCache getDecisionCache();
	
	PolicyDecisionAuditor getDecisionAuditor();
	
	Result requestDecision(RequestContext req);
	
	RequestContextHandler getRequestHandlers();
}
