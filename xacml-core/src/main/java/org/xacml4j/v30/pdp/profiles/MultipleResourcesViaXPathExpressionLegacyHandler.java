package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.types.XacmlTypes;


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
					Result.indeterminate(
							Status.syntaxError()
							.message("Found repeating categories in the request")
							.build())
							.includeInResultAttr(request.getIncludeInResultAttributes()).build());
		}
		Category resource = request.getOnlyAttributes(Categories.RESOURCE);
		if(resource == null){
			return handleNext(request, context);
		}
		Entity entity = resource.getEntity();
		Collection<AttributeExp> resourceId = entity.getAttributeValues(RESOURCE_ID_ATTRIBUTE,
				XacmlTypes.XPATH);
		if(resourceId.isEmpty()){
			return handleNext(request, context);
		}
		if(resourceId.size() > 1){
			return Collections.singleton(
					Result.indeterminate(
							Status
							.syntaxError()
							.message("Found more than AttributeId=\"%s\" " +
									"value of type=\"%s\"", RESOURCE_ID_ATTRIBUTE, 
									XacmlTypes.XPATH).build())
							.includeInResultAttr(request.getIncludeInResultAttributes())
							.build());
		}
		Collection<Category> attributes = new LinkedList<Category>();
		for(Category attrs : request.getAttributes())
		{
			if(attrs.getCategoryId().equals(Categories.RESOURCE)){
				Collection<Attribute> resourceAttr = new LinkedList<Attribute>();
				Entity en = attrs.getEntity();
				for(Attribute attr : en.getAttributes()){
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
				attributes.add(Category
						.builder(attrs.getCategoryId())
						.entity(Entity.builder().content(entity.getContent()).attributes(resourceAttr).build())
						.build());
				continue;
			}
			attributes.add(attrs);
		}
		return handleNext(new RequestContext(
				request.isReturnPolicyIdList(), attributes), context);
	}

}
