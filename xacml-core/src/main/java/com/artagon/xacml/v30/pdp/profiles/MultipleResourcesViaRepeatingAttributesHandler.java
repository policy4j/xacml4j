package com.artagon.xacml.v30.pdp.profiles;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.pdp.AbstractRequestContextHandler;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.Result;
import com.google.common.collect.Sets;

final class MultipleResourcesViaRepeatingAttributesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories";
	
	public MultipleResourcesViaRepeatingAttributesHandler(){
		super(FEATURE_ID);
	}
	
	@Override
	public Collection<Result> handle(RequestContext request, 
			PolicyDecisionPointContext context) 
	{
		if(!request.containsRepeatingCategories()){
			return handleNext(request, context);
		}
		List<Set<Attributes>> byCategory = new LinkedList<Set<Attributes>>();
		for(AttributeCategory categoryId : request.getCategories()){
			Collection<Attributes> attributes = request.getAttributes(categoryId);
			if(attributes == null ||
					attributes.isEmpty()){
				continue;
			}
			byCategory.add(new LinkedHashSet<Attributes>(attributes));
		}
		Collection<Result> results = new LinkedList<Result>();
		Set<List<Attributes>> cartesian = Sets.cartesianProduct(byCategory);
		for(List<Attributes> requestAttr : cartesian)
		{	
			RequestContext req = new RequestContext(request.isReturnPolicyIdList(), requestAttr);
			results.addAll(handleNext(req, context));
		}
		return results;
	}
}
