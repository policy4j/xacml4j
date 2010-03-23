package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.PolicyDecisionPoint;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProcessingException;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;

public class MultipleAttributesByReferenceProfile extends BaseRequestContextProfile
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";
	
	private PolicyDecisionPoint pdp;
	
	public MultipleAttributesByReferenceProfile(){
		super(ID);
	}
	
	public Response process(Request request) 
	{
		Collection<Result> results = new LinkedList<Result>();
		for(RequestReference ref : request.getRequestReferences())
		{
			try
			{
				Request req = resolveAttributes(request, ref);
				results.add(pdp.evaluate(req));
			}catch(RequestProcessingException e){
				results.add(new Result(e.getStatusCode()));
			}
		}
		return new Response(results);
	}
	
	private Request resolveAttributes(Request request, 
			RequestReference requestRef) throws RequestProcessingException
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<Attributes> resolved = new LinkedList<Attributes>();
		for(AttributesReference ref : requestRef.getReferencedAttributes()){
			Attributes attributes = request.getReferencedAttributes(ref);
			if(attributes == null){
				results.add(new Result(
						new Status(StatusCode.createSyntaxError(), 
						"Failed to resolve attribute reference", 
						ref.getReferenceId())));
				break;
			}
			resolved.add(attributes);
		}
		return new Request(request.isReturnPolicyIdList(), resolved);
	}
	
}
