package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.types.XPathExpType;


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
						Attribute selector =
								Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
								.issuer(attr.getIssuer())
								.includeInResult(attr.isIncludeInResult())
								.values(attr.getValues())
								.build();
						resourceAttr.add(selector);
						continue;
					}
					resourceAttr.add(attr);
				}
				attributes.add(Attributes
						.builder(attrs.getCategory())
						.content(attrs.getContent())
						.attributes(resourceAttr)
						.build());
				continue;
			}
			attributes.add(attrs);
		}
		return handleNext(new RequestContext(
				request.isReturnPolicyIdList(), attributes), context);
	}

}
