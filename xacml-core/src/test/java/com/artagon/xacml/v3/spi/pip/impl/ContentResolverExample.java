package com.artagon.xacml.v3.spi.pip.impl;

import java.util.Map;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeDesignator;
import com.artagon.xacml.v3.sdk.XacmlAttributeSelector;
import com.artagon.xacml.v3.sdk.XacmlContentResolverDescriptor;

public class ContentResolverExample 
{
	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Map<String, BagOfAttributeValues> resolve(
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", dataType="boolean") BagOfAttributeValues k1, 
			@XacmlAttributeSelector(category="test", xpath="/test", dataType="boolean") BagOfAttributeValues k2)
	{
		return null;
	}
}
