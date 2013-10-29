package org.xacml4j.v30;

import org.xacml4j.v30.spi.xpath.XPathProvider;

public interface EvaluationContextFactory
{
	XPathProvider getXPathProvider();
	EvaluationContext createContext(RequestContext request);
}
