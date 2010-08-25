package com.artagon.xacml.v3.pdp.profiles;


public class MultipleDecisionProfileHandler extends RequestContextHandlerChain
{
	public MultipleDecisionProfileHandler()
	{
		super(new MultipleDecisionRequestReferencesHandler(), 
				new MultipleDecisionRepeatingAttributesHandler(),
				new LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler(),
				new MultipleDecisionXPathExpressionHandler());
	}
	
}
