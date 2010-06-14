package com.artagon.xacml.v3.profiles;

import com.artagon.xacml.v3.policy.spi.XPathProvider;

public class MultipleDecisionProfileHandler extends CompositeRequestProfileHandler
{
	public MultipleDecisionProfileHandler(XPathProvider xpathProvider)
	{
		super(new MultipleDecisionRequestReferencesHandler(), 
				new MultipleDecisionRepeatingAttributesHandler(),
				new LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler(),
				new MultipleDecisionProfileHandler(xpathProvider));
	}
	
}
