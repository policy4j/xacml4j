package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.impl.DefaultRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

public class MultipleResourcesHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
		
	public MultipleResourcesHandler() {
		super(ID);
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		Collection<Attributes> resources = request.getAttributes(AttributeCategoryId.RESOURCE);
		if(resources.size() <= 1){
			return handleNext(request, pdp);
		}
		Collection<Attributes> nonResourceAttributes = Collections2.filter(request.getAttributes(), 
				new Predicate<Attributes>() {
			@Override
			public boolean apply(Attributes arg) {
				return !arg.getCategoryId().equals(AttributeCategoryId.RESOURCE);
			
			}
		});
		Collection<Result> results = new LinkedList<Result>();
		for(Attributes resource : resources)
		{ 
			Collection<Attributes> attr = new LinkedList<Attributes>();
			attr.add(resource);
			Iterables.addAll(attr, nonResourceAttributes);
			results.addAll(handleNext(new DefaultRequest(request.isReturnPolicyIdList(),  attr), pdp));
		}
		return results;
	}
}
