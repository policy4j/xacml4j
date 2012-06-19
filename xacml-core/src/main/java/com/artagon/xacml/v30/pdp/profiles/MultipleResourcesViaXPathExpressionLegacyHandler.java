package com.artagon.xacml.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.pdp.AbstractRequestContextHandler;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.types.XPathExpType;

final class MultipleResourcesViaXPathExpressionLegacyHandler 
	extends AbstractRequestContextHandler
{
	final static String FEATURE_ID = "urn:oasis:names:tc:xacml:2.0:profile:multiple:xpath-expression";
	
	final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	public MultipleResourcesViaXPathExpressionLegacyHandler(){
		super(FEATURE_ID);
	}
	
	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context) 
	{
		if(request.containsRepeatingCategories()){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError("Found repeating categories in the request")
							.includeInResultAttr(request.getIncludeInResultAttributes()).build());
		}
		Attributes resource = request.getOnlyAttributes(AttributeCategories.RESOURCE);
		if(resource == null){
			return handleNext(request, context);
		}
		Collection<AttributeExp> resourceId = resource.getAttributeValues(RESOURCE_ID_ATTRIBUTE, 
				XPathExpType.XPATHEXPRESSION);
		if(resourceId.isEmpty()){
			return handleNext(request, context);
		}
		if(resourceId.size() > 1){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError(
							"Found more than AttributeId=\"%s\" " +
							"value of type=\"%s\"", RESOURCE_ID_ATTRIBUTE, 
							XPathExpType.XPATHEXPRESSION)
							.includeInResultAttr(request.getIncludeInResultAttributes())
							.build());
		}
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		for(Attributes attrs : request.getAttributes())
		{
			if(attrs.getCategory().equals(AttributeCategories.RESOURCE)){
				Collection<Attribute> resourceAttr = new LinkedList<Attribute>();
				for(Attribute attr : attrs.getAttributes()){
					if(attr.getAttributeId().equals(RESOURCE_ID_ATTRIBUTE))
					{
						Attribute selector = new Attribute(
								MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR, 
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
		return handleNext(new RequestContext(
				request.isReturnPolicyIdList(), attributes), context);
	}

}
