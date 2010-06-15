package com.artagon.xacml.v3.policy.spi;

import java.util.Map;

import com.artagon.xacml.v3.PolicyIdentifier;

public class Registry 
{
	private Map<PolicyIdentifier, AttributeResolver> attributeResolvers;
	private Map<PolicyIdentifier, ContentResolver> contentResolvers;
	
	public void addResolver(PolicyIdentifier policyIdentifier, AttributeResolver resolver)
	{
	}
}
