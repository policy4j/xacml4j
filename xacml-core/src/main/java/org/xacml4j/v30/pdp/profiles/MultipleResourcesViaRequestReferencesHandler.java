package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.LinkedList;

import org.xacml4j.v30.Attributes;
import org.xacml4j.v30.AttributesReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.pdp.RequestSyntaxException;


final class MultipleResourcesViaRequestReferencesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";

	public MultipleResourcesViaRequestReferencesHandler() {
		super(FEATURE_ID);
	}

	@Override
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
				results.add(
						Result
						.createIndeterminate(e.getStatus())
						.includeInResultAttr(request.getIncludeInResultAttributes())
						.build());
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
