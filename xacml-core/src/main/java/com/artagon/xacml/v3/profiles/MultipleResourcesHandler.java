package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.policy.impl.DefaultAttributes;
import com.artagon.xacml.v3.policy.impl.DefaultRequest;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class MultipleResourcesHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
	
	private final static Logger log = LoggerFactory.getLogger(MultipleResourcesHandler.class);
	
	public MultipleResourcesHandler() {
		super(ID);
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		Collection<DefaultAttributes> resources = request.getAttributes(AttributeCategoryId.RESOURCE);
		if(resources.size() <= 1){
			return handleNext(request, pdp);
		}
		Collection<DefaultAttributes> otherAttributes = Collections2.filter(request.getAttributes(), 
				new Predicate<DefaultAttributes>() {
			@Override
			public boolean apply(DefaultAttributes arg) {
				boolean filter = !arg.getCategoryId().equals(AttributeCategoryId.RESOURCE);
				if(filter){
					log.debug("Filtering attributes=\"{}\"", arg);
				}
				return filter;
			}
		});
		Collection<Result> results = new LinkedList<Result>();
		for(DefaultAttributes resource : resources)
		{ 
			Collection<DefaultAttributes> attr = new LinkedList<DefaultAttributes>(otherAttributes);
			attr.add(resource);
			results.addAll(handleNext(new DefaultRequest(request.isReturnPolicyIdList(),  attr), pdp));
		}
		return results;
	}
}
