package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.spi.xpath.XPathProvider;

public interface EvaluationContextFactory 
{
	XPathProvider getXPathProvider();
	EvaluationContext createContext(RequestContext request);
}
