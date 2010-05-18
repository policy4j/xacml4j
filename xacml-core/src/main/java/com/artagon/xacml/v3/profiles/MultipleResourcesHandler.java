package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.impl.DefaultRequest;

public class MultipleResourcesHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";
		
	public MultipleResourcesHandler() {
		super(ID);
	}
	
	@Override
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		Map<AttributeCategoryId, Collection<Attributes>> attributes = request.getAttributes();
		Collection<Result> results = new LinkedList<Result>();
		Iterator<AttributeCategoryId> it = attributes.keySet().iterator();
		while(it.hasNext()){
			AttributeCategoryId category = it.next();
			for(Attributes a : attributes.get(category))
			{
				Collection<Attributes> cartesian = new LinkedList<Attributes>();
				cartesian.add(a);
				cartesian(it, cartesian, attributes);
				results.addAll(handleNext(new DefaultRequest(request.isReturnPolicyIdList(), cartesian), pdp));
			}
		}
		return results;
	}
	
	private void cartesian(Iterator<AttributeCategoryId> it, 
			Collection<Attributes> cartesian, Map<AttributeCategoryId, Collection<Attributes>> all)
	{
		if(!it.hasNext()){
			return;
		}
		AttributeCategoryId category = it.next();
		for(Attributes a : all.get(category)){
			cartesian.add(a);
			cartesian(it, cartesian, all);
		}		
	}
}
