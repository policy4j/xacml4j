package com.artagon.xacml.v3.pdp;


import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;

public interface PolicyDecisionPointContext
{
	XPathProvider getXPathProvider();	
	
	EvaluationContext createEvaluationContext(RequestContext req);
	
	PolicyDomain getPolicyDomain();
	
	PolicyDecisionCache getDecisionCache();
	
	PolicyDecisionAuditor getAuditor();
	
	PolicyDecisionCallback getPolicyDecisionCallback();
	
	RequestContextHandlerChain getRequestHandlers();
}
