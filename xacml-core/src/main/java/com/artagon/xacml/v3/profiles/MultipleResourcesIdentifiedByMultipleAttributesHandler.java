package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.google.common.collect.Sets;

public class MultipleResourcesIdentifiedByMultipleAttributesHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
		
	public MultipleResourcesIdentifiedByMultipleAttributesHandler() {
		super(ID);
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		List<Set<Attributes>> byCategory = new LinkedList<Set<Attributes>>();
		for(AttributeCategoryId categoryId : request.getCategories()){
			Collection<Attributes> attributes = request.getAttributes(categoryId);
			if(attributes == null ||
					attributes.isEmpty()){
				continue;
			}
			byCategory.add(new HashSet<Attributes>(attributes));
		}
		Collection<Result> results = new LinkedList<Result>();
		Set<List<Attributes>> cartesian = Sets.cartesianProduct(byCategory);
		for(List<Attributes> requestAttr : cartesian)
		{	
			Request req = new Request(request.isReturnPolicyIdList(), requestAttr);
			results.addAll(handleNext(req, pdp));
		}
		return results;
	}
}
