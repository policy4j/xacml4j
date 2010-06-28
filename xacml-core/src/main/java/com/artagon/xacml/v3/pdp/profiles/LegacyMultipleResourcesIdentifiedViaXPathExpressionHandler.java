package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler extends AbstractRequestProfileHandler
{
	private final static Logger log = LoggerFactory.getLogger(LegacyMultipleResourcesIdentifiedViaXPathExpressionHandler.class);
	
	final static String RESOURCE_ID_ATTRIBUTE = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
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
		Collection<AttributeValue> resourceId = resource.getAttributeValues(RESOURCE_ID_ATTRIBUTE, 
				XacmlDataTypes.XPATHEXPRESSION.getType());
		if(resourceId.isEmpty()){
			return handleNext(request, pdp);
		}
		if(resourceId.size() > 1){
			return Collections.singleton(
					Result.createIndeterminateSyntaxError(
							"Found more than AttributeId=\"%s\" " +
							"value of type=\"%s\"", RESOURCE_ID_ATTRIBUTE, 
							XacmlDataTypes.XPATHEXPRESSION.getTypeId()));
		}
		Collection<Attributes> attributes = new LinkedList<Attributes>();
		for(Attributes attrs : request.getAttributes())
		{
			if(attrs.getCategoryId().equals(AttributeCategoryId.RESOURCE)){
				Collection<Attribute> resourceAttr = new LinkedList<Attribute>();
				for(Attribute attr : attrs.getAttributes()){
					if(attr.getAttributeId().equals(RESOURCE_ID_ATTRIBUTE))
					{
						log.debug("Transforming resource attr=\"{}\"", attr);
						Attribute selector = new Attribute(MultipleDecisionXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR, 
								attr.getValues());
						resourceAttr.add(selector);
						continue;
					}
					resourceAttr.add(attr);
				}
				attributes.add(new Attributes(attrs.getCategoryId(), attrs.getContent(), resourceAttr));
				continue;
			}
			attributes.add(attrs);
		}
		return handleNext(new Request(request.isReturnPolicyIdList(), attributes), pdp);
	}

}
