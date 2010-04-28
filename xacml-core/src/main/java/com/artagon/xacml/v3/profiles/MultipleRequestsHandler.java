package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.PolicyDecisionCallback;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.policy.impl.DefaultRequest;

public class MultipleRequestsHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";
	
	public MultipleRequestsHandler(){
		super(ID);
	}
	
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<RequestReference> references = request.getRequestReferences();
		if(references.isEmpty()){
			return handleNext(request, pdp);
		}
		for(RequestReference ref : references){
			Request resolvedRequest = resolveAttributes(request, ref);
			results.addAll(handleNext(resolvedRequest, pdp));
		}
		return results;
	}
	
	private Request resolveAttributes(Request req, 
			RequestReference reqRef)
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<Attributes> resolved = new LinkedList<Attributes>();
		for(AttributesReference ref : reqRef.getReferencedAttributes()){
			Attributes attributes = req.getReferencedAttributes(ref);
			if(attributes == null){
				results.add(new Result(
						new Status(StatusCode.createSyntaxError(), 
						"Failed to resolve attribute reference", 
						ref.getReferenceId())));
				break;
			}
			resolved.add(attributes);
		}
		return new DefaultRequest(req.isReturnPolicyIdList(), resolved);
	}
	
}
