package com.artagon.xacml.v3.pdp.profiles;

import java.util.Collection;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.RequestContextHandlerChain;


public class MultipleResourcesHandler extends RequestContextHandlerChain
{	
	public MultipleResourcesHandler()
	{
		super(new MultipleResourcesViaRequestReferencesHandler(), 
				new MultipleResourcesViaRepeatingAttributesHandler(),
				new MultipleResourcesViaXPathExpressionLegacyHandler(),
				new MultipleResourcesViaXPathExpressionHandler());
	}

	@Override
	protected Collection<Result> postProcessResults(RequestContext req,
			Collection<Result> results) {
		if(!req.isCombinedDecision()){
			return results;
		}
		// TODO implement request combine decision profile
		return results;
	}
}
