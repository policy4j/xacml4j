package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler extends AbstractRequestProfileHandler
{
	final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	final static String MULTIPLE_CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		if(request.hasRepeatingCategories()){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError(
							"Found repeating categories in the request"));
		}
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		if(resource == null){
			return handleNext(request, pdp);
		}
		Collection<AttributeValue> resourceId = resource.getAttributeValues(
				RESOURCE_ID_ATTRIBUTE, 
				XacmlDataTypes.XPATHEXPRESSION.getType());
		if(resourceId.isEmpty()){
			return handleNext(request, pdp);
		}
		return handleNext(request, pdp);
	}
}
