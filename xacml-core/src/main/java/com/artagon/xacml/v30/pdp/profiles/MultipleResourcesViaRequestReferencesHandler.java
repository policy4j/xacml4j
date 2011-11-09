package com.artagon.xacml.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v30.pdp.AbstractRequestContextHandler;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.AttributesReference;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointContext;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.RequestReference;
import com.artagon.xacml.v30.pdp.RequestSyntaxException;
import com.artagon.xacml.v30.pdp.Result;

final class MultipleResourcesViaRequestReferencesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";
	
	public MultipleResourcesViaRequestReferencesHandler() {
		super(FEATURE_ID);
	}
	
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context) 
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<RequestReference> references = request.getRequestReferences();
		if(references.isEmpty()){
			return handleNext(request, context);
		}
		for(RequestReference ref : references){
			try{
				RequestContext resolvedRequest = resolveAttributes(request, ref);
				results.addAll(handleNext(resolvedRequest, context));
			}catch(RequestSyntaxException e){
				results.add(new Result(Decision.INDETERMINATE, e.getStatus(), 
						request.getIncludeInResultAttributes(), 
						Collections.<Attributes>emptyList()));
			}
		}
		return results;
	}
	
	private RequestContext resolveAttributes(RequestContext req, 
			RequestReference reqRef) throws RequestSyntaxException
	{
		Collection<Attributes> resolved = new LinkedList<Attributes>();
		for(AttributesReference ref : reqRef.getReferencedAttributes()){
			Attributes attributes = req.getReferencedAttributes(ref);
			if(attributes == null){
				throw new RequestSyntaxException(
						"Failed to resolve attribute reference", 
						ref.getReferenceId());
			}
			resolved.add(attributes);
		}
		return new RequestContext(req.isReturnPolicyIdList(), resolved);
	}
	
}
