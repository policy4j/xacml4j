package com.artagon.xacml.v3.spi.pip;

import java.util.Map;

import com.artagon.xacml.v3.BagOfAttributeValues;

public class AttributesResolverExample 
{
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="boolean", id="testId"),
				@XacmlAttributeDescriptor(dataType="boolean", id="testId"),
				@XacmlAttributeDescriptor(dataType="boolean", id="testId"),
				@XacmlAttributeDescriptor(dataType="boolean", id="testId")
	})
	public Map<String, BagOfAttributeValues> resolve(
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", dataType="boolean") BagOfAttributeValues k1, 
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", dataType="boolean") BagOfAttributeValues k2)
	{
		return null;
	}
}
