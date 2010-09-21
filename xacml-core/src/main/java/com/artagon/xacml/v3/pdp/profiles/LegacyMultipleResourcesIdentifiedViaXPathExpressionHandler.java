package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.types.XPathExpressionType;

public class LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler extends AbstractRequestContextHandler
{
	final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionCallback pdp) 
	{
		if(request.hasRepeatingCategories()){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError(
							request.getIncludeInResultAttributes(),
							"Found repeating categories in the request"));
		}
		Attributes resource = request.getOnlyAttributes(AttributeCategoryId.RESOURCE);
		if(resource == null){
			return handleNext(request, pdp);
		}
		Collection<AttributeValue> resourceId = resource.getAttributeValues(RESOURCE_ID_ATTRIBUTE, 
				XPathExpressionType.Factory.getInstance());
		if(resourceId.isEmpty()){
			return handleNext(request, pdp);
		}
		if(resourceId.size() > 1){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError(
							request.getIncludeInResultAttributes(),
							"Found more than AttributeId=\"%s\" " +
							"value of type=\"%s\"", RESOURCE_ID_ATTRIBUTE, 
							XPathExpressionType.Factory.getInstance()));
		}
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		for(Attributes attrs : request.getAttributes())
		{
			if(attrs.getCategory().equals(AttributeCategoryId.RESOURCE)){
				Collection<Attribute> resourceAttr = new LinkedList<Attribute>();
				for(Attribute attr : attrs.getAttributes()){
					if(attr.getAttributeId().equals(RESOURCE_ID_ATTRIBUTE))
					{
						Attribute selector = new Attribute(
								MultipleDecisionXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR, 
								attr.getIssuer(), true, attr.getValues());
						resourceAttr.add(selector);
						continue;
					}
					resourceAttr.add(attr);
				}
				attributes.add(new Attributes(attrs.getCategory(), attrs.getContent(), resourceAttr));
				continue;
			}
			attributes.add(attrs);
		}
		return handleNext(new RequestContext(request.isReturnPolicyIdList(), attributes), pdp);
	}

}
