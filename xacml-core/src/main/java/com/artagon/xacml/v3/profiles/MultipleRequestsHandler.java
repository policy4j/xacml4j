package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.ContextFactory;
import com.artagon.xacml.v3.ContextSyntaxException;
import com.artagon.xacml.v3.PolicyDecisionCallback;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.Result;

public class MultipleRequestsHandler extends AbstractRequestProfileHandler
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";
	
	public MultipleRequestsHandler(ContextFactory contextFactory){
		super(ID, contextFactory);
	}
	
	public Collection<Result> handle(Request request, PolicyDecisionCallback pdp) 
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<RequestReference> references = request.getRequestReferences();
		if(references.isEmpty()){
			return handleNext(request, pdp);
		}
		for(RequestReference ref : references){
			try{
				Request resolvedRequest = resolveAttributes(request, ref);
				results.addAll(handleNext(resolvedRequest, pdp));
			}catch(ContextSyntaxException e){
				results.add(new Result(e.getStatus()));
			}
		}
		return results;
	}
	
	private Request resolveAttributes(Request req, 
			RequestReference reqRef) throws ContextSyntaxException
	{
		Collection<Attributes> resolved = new LinkedList<Attributes>();
		for(AttributesReference ref : reqRef.getReferencedAttributes()){
			Attributes attributes = req.getReferencedAttributes(ref);
			if(attributes == null){
				throw new ContextSyntaxException(
						"Failed to resolve attribute reference", 
						ref.getReferenceId());
			}
			resolved.add(attributes);
		}
		return getContextFactory().createRequest(req.isReturnPolicyIdList(), resolved);
	}
	
}
