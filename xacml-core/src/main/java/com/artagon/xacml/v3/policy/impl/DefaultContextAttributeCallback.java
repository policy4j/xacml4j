package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.spi.ContextAttributeCallback;

public class DefaultContextAttributeCallback implements ContextAttributeCallback
{
	private Request request;
	
	public DefaultContextAttributeCallback(Request request)
	{
		Preconditions.checkNotNull(request);
		this.request = request;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends AttributeValue> Attribute<V> getAttribute(
			AttributeCategoryId categoryId, String attributeId) {
		Attributes attributes = request.getAttributes(categoryId);
		return (attributes == null)?null:attributes.getAttribute(attributeId);
	}
}
