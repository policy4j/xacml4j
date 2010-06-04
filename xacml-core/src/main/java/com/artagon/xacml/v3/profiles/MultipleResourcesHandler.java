package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.context.Attributes;
import com.artagon.xacml.v3.context.ContextFactory;
import com.artagon.xacml.v3.context.ContextSyntaxException;
import com.artagon.xacml.v3.context.Request;
import com.artagon.xacml.v3.context.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCallback;
import com.google.common.collect.Sets;

public class MultipleResourcesHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
		
	public MultipleResourcesHandler(ContextFactory contextFactory) {
		super(ID, contextFactory);
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		ContextFactory factory = getContextFactory();
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
			try{
				Request req = factory.createRequest(request.isReturnPolicyIdList(), requestAttr);
				results.addAll(handleNext(req, pdp));
			}catch(ContextSyntaxException e){
				results.add(new Result(e.getStatus()));
			}
		}
		return results;
	}
}
