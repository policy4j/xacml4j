package com.artagon.xacml.v3.spi.resolver;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.AttributesCallback;
import com.google.common.collect.ImmutableSet;

public class EnviromentAttributeLocator extends BaseAttributeResolver
{	
	public EnviromentAttributeLocator(){
		super(ImmutableSet.of(AttributeCategoryId.ENVIRONMENT), 
				ImmutableSet.of(
						"urn:oasis:names:tc:xacml:1.0:environment:current-time",
						"urn:oasis:names:tc:xacml:1.0:environment:current-date",
						"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime"));
	}

	@Override
	public BagOfAttributeValues<? extends AttributeValue> resolve(
			AttributeDesignator ref, AttributesCallback callback) 
	{
		return ref.getDataType().bagOf().createEmpty();
	}
}
