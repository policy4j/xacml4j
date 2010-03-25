package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProcessingException;
import com.artagon.xacml.v3.RequestProcessingProfile;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;

public class MultipleResourcesByReferenceProfile extends BaseRequestContextProfile
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";
	
	private RequestProcessingProfile profile;
	public MultipleResourcesByReferenceProfile(RequestProcessingProfile profile){
		super(ID);
		this.profile = profile;
	}
	
	public Collection<Result> process(Request request) 
	{
		Collection<Result> results = new LinkedList<Result>();
		for(RequestReference ref : request.getRequestReferences())
		{
			try
			{
				Request resolvedRequest = resolveAttributes(request, ref);
				results.addAll(profile.process(resolvedRequest));
			}catch(RequestProcessingException e){
				results.add(new Result(e.getStatusCode()));
			}
		}
		return results;
	}
	
	private Request resolveAttributes(Request req, 
			RequestReference reqRef) throws RequestProcessingException
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
		return new Request(req.isReturnPolicyIdList(), resolved);
	}
	
}
