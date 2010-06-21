package com.artagon.xacml.v3.pdp.profiles;

import com.artagon.xacml.v3.spi.XPathProvider;

public class MultipleDecisionProfileHandler extends RequestProfileHandlerChain
{
	public MultipleDecisionProfileHandler(XPathProvider xpathProvider)
	{
		super(new MultipleDecisionRequestReferencesHandler(), 
				new MultipleDecisionRepeatingAttributesHandler(),
				new LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler(),
				new MultipleDecisionProfileHandler(xpathProvider));
	}
	
}
