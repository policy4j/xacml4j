package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.Entity;
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
	public Entity getEntity(AttributeCategory category) {
		return request.getOnlyEntity(category);
	}
	
	
}
