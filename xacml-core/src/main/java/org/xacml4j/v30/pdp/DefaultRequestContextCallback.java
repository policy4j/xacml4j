package org.xacml4j.v30.pdp;

import java.util.Collection;

import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.RequestContext;

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
	public BagOfAttributeExp getAttributeValue(
			AttributeCategory category,
			String attributeId,
			AttributeExpType dataType,
			String issuer) {
		Collection<AttributeExp> values = request.getAttributeValues(
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
