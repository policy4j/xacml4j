package com.artagon.xacml.v3.policy.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.PolicyIdentifier;

public class Registry 
{
	private Map<PolicyIdentifier, AttributeResolver> attributeResolvers;
	private Map<PolicyIdentifier, ContentResolver> contentResolvers;
	
	public void addResolver(PolicyIdentifier policyIdentifier, AttributeResolver resolver)
	{
	}
}
