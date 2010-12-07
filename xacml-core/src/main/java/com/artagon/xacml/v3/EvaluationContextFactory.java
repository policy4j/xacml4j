package com.artagon.xacml.v3;

import com.artagon.xacml.v3.context.RequestContext;
import com.artagon.xacml.v3.spi.XPathProvider;



public interface EvaluationContextFactory 
{
	XPathProvider getXPathProvider();
	EvaluationContext createContext(RequestContext request);
}
