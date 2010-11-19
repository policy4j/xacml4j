package com.artagon.xacml.v3.spi.pip;

import java.util.Map;

import com.artagon.xacml.v3.BagOfAttributeValues;

public class ContentResolverExample 
{
	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Map<String, BagOfAttributeValues> resolve(
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", dataType="boolean")BagOfAttributeValues k1, 
			@XacmlAttributeSelector(category="test", xpath="/test", dataType="boolean")BagOfAttributeValues k2)
	{
		return null;
	}
}
