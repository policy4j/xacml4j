package org.xacml4j.v30.pdp.profiles;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
