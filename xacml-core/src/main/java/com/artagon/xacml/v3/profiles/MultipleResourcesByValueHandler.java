package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.PolicyDecisionPoint;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class MultipleResourcesByValueHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
	
	public MultipleResourcesByValueHandler(PolicyDecisionPoint pdp) {
		super(ID, pdp);
	}
	
	@Override
	public Collection<Result> handle(Request request) 
	{
		Collection<Attributes> resources = request.getAttributes(AttributeCategoryId.RESOURCE);
		if(resources.size() <= 1){
			return handleNext(request);
		}
		Collection<Attributes> otherAttributes = Collections2.filter(request.getAttributes(), 
				new Predicate<Attributes>() {
			@Override
			public boolean apply(Attributes arg) {
				return arg.getCategoryId().equals(AttributeCategoryId.RESOURCE);
			}
		});
		Collection<Result> results = new LinkedList<Result>();
		for(Attributes resource : resources){ 
			Collection<Attributes> attr = new LinkedList<Attributes>(otherAttributes);
			attr.add(resource);
			results.addAll(handleNext(new Request(request.isReturnPolicyIdList(),  attr)));
		}
		return results;
	}
}
