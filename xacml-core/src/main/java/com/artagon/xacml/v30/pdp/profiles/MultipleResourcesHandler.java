package com.artagon.xacml.v30.pdp.profiles;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.Advice;
import com.artagon.xacml.v30.Obligation;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandlerChain;


public class MultipleResourcesHandler extends RequestContextHandlerChain
{	
	private final static Logger log = LoggerFactory.getLogger(MultipleResourcesHandler.class);
	
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
		if(results.size() == 1){
			return results;
		}
		Result prev = null;
		for(Result r : results)
		{
			Collection<Advice> advice = r.getAssociatedAdvice();
			if(!advice.isEmpty()){
				return Collections.singleton(
						Result.createIndeterminateProcessingError().build());
			}
			Collection<Obligation> obligation = r.getObligations();
			if(!obligation.isEmpty()){
				return Collections.singleton(
						Result.createIndeterminateProcessingError().build());
			}
			if(prev != null){
				if(r.getDecision().isIndeterminate()){
					if(!prev.getDecision().isIndeterminate()){
						return Collections.singleton(
								Result.createIndeterminateProcessingError().build());
					}
				}
				if(!r.getDecision().equals(prev.getDecision())){
					if(log.isDebugEnabled()){
						log.debug("Previous decision result=\"{}\" " +
								"can not be combined with the current result=\"{}\"", prev, r);
					}
					return Collections.singleton(
							Result.createIndeterminateProcessingError().build());
				}
			}
			prev = r;
		}
		if(prev == null){
			return Collections.singleton(
					Result.createIndeterminateProcessingError().build());
		}
		return Collections.singleton(Result.createOk(prev.getDecision()).build());
	}
}
