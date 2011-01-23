package com.artagon.xacml.v30.pdp;

import java.util.Collection;

import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.RequestContext;
import com.google.common.base.Preconditions;

public class DefaultRequestContextCallback 
	implements RequestContextCallback
{
	private RequestContext request;

	public DefaultRequestContextCallback(RequestContext request)
	{
		Preconditions.checkNotNull(request);
		Preconditions.checkArgument(
				!request.containsRepeatingCategories(), 
				"RequestContext has repeating attributes categories");
		Preconditions.checkArgument(
				!request.containsRequestReferences(), 
				"RequestContext contains multiple request references");
		this.request = request;
	}
	
	@Override
	public BagOfAttributeValues getAttributeValue(
			AttributeCategory category, 
			String attributeId, 
			AttributeValueType dataType, 
			String issuer) {
		Collection<AttributeValue> values = request.getAttributeValues(
				category, 
				attributeId, 
				dataType, 
				issuer);
		return dataType.bagOf(values);
	}
	
	@Override
	public Node getContent(AttributeCategory category) {
		Preconditions.checkNotNull(category);
		return request.getOnlyContent(category);
	}
	
}
