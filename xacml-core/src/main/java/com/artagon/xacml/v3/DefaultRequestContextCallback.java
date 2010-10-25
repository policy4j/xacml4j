package com.artagon.xacml.v3;

import java.util.Collection;

import org.w3c.dom.Node;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class DefaultRequestContextCallback implements RequestContextCallback
{
	private RequestContext request;
	
	public DefaultRequestContextCallback(RequestContext req){
		Preconditions.checkArgument(req != null);
		Preconditions.checkArgument(!req.containsRepeatingCategories(), 
				"RequestContext has repeating attributes categories");
		this.request = req;
	}
	
	@Override
	public BagOfAttributeValues getAttributeValues(
			AttributeCategory category, String attributeId, AttributeValueType dataType, String issuer) {
		Collection<Attributes> attributes = request.getAttributes(category);
		Attributes  found = Iterables.getOnlyElement(attributes);
		return dataType.bagType().create(found.getAttributeValues(attributeId, issuer, dataType));
	}

	@Override
	public BagOfAttributeValues getAttributeValues(
			AttributeCategory category, String attributeId, AttributeValueType dataType) {
		return getAttributeValues(category, attributeId, dataType, null);
	}

	@Override
	public <AV extends AttributeValue> AV getAttributeValue(
			AttributeCategory categoryId, String attributeId,
			AttributeValueType dataType, String issuer) {
		BagOfAttributeValues bag = getAttributeValues(categoryId, attributeId, dataType, issuer);
		return bag.isEmpty()?null:bag.<AV>value();
	}

	@Override
	public <AV extends AttributeValue> AV getAttributeValue(
			AttributeCategory categoryId, 
			String attributeId,
			AttributeValueType dataType) {
		BagOfAttributeValues bag = getAttributeValues(categoryId, attributeId, dataType);
		return bag.isEmpty()?null:bag.<AV>value();
	}

	@Override
	public Node getContent(AttributeCategory category) {
		return request.getOnlyContent(category);
	}		
}

