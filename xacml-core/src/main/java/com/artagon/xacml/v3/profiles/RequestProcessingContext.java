package com.artagon.xacml.v3.profiles;

import com.artagon.xacml.v3.policy.spi.XPathProvider;

public interface RequestProcessingContext 
{
	XPathProvider getXPathProvider();
}
