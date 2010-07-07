package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.artagon.xacml.util.Cartesian;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;

final class MultipleDecisionRepeatingAttributesHandler extends AbstractRequestProfileHandler
{
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		if(!request.hasRepeatingCategories()){
			return handleNext(request, pdp);
		}
		List<Set<Attributes>> byCategory = new LinkedList<Set<Attributes>>();
		for(AttributeCategoryId categoryId : request.getCategories()){
			Collection<Attributes> attributes = request.getAttributes(categoryId);
			if(attributes == null ||
					attributes.isEmpty()){
				continue;
			}
			byCategory.add(new LinkedHashSet<Attributes>(attributes));
		}
		Collection<Result> results = new LinkedList<Result>();
		List<Set<Attributes>> cartesian = Cartesian.cartesianProduct(byCategory);
		for(Set<Attributes> requestAttr : cartesian)
		{	
			Request req = new Request(request.isReturnPolicyIdList(), requestAttr);
			results.addAll(handleNext(req, pdp));
		}
		return results;
	}
}
