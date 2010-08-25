package com.artagon.xacml.spring;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v3.spi.pip.BaseAttributeResolver;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;

public class TestAttributeResolver extends BaseAttributeResolver
{
	public TestAttributeResolver(){
		super(AttributeResolverDescriptorBuilder.create(AttributeCategoryId.ENVIRONMENT).
				attribute("test").build());
	}
	
	@Override
	public BagOfAttributeValues<? extends AttributeValue> resolve(
			PolicyInformationPointContext context, AttributeDesignator ref,
			RequestContextAttributesCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
