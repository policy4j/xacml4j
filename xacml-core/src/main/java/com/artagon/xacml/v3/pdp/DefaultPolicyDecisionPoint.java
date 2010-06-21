package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.pdp.profiles.RequestProfileHandlerChain;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class DefaultPolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);
	
	private EvaluationContextFactory factory;
	private PolicyRepository policyRepository;
	private RequestProfileHandlerChain requestProcessingPipeline;
	
	public DefaultPolicyDecisionPoint(
			List<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyRepository policyRepository)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policyRepository);
		this.factory = factory;
		this.policyRepository = policyRepository;
		this.requestProcessingPipeline = new RequestProfileHandlerChain(handlers);
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyRepository policyRepostory)
	{
		this(Collections.<RequestProfileHandler>emptyList(), factory, policyRepostory);
	}

	@Override
	public Response decide(Request request)
	{
		return new Response(requestProcessingPipeline.handle(request, this));			
	}

	@Override
	public Result requestDecision(Request request) 
	{
		EvaluationContext context = factory.createContext(request);
		Collection<CompositeDecisionRule> applicable = policyRepository.findApplicable(context);
		if(applicable.size() == 0){
			if(log.isDebugEnabled()){
				log.debug("Found no applicable policies");
			}
			return new Result(Decision.NOT_APPLICABLE, 
					new Status(StatusCode.createOk()));
		}
		if(applicable.size() > 1){
			log.debug("Found more than one applicable policy");
			return new Result(Decision.NOT_APPLICABLE, 
					new Status(StatusCode.createProcessingError()));
		}
		CompositeDecisionRule policy = Iterables.getOnlyElement(applicable);
		EvaluationContext policyContext = policy.createContext(context);
		Decision decision = policy.evaluateIfApplicable(policyContext);
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					new Status(StatusCode.createOk()));
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status));
		}
		return new Result(
				decision, 
				new Status(StatusCode.createOk()),
				context.getAdvices(), 
				context.getObligations(), 
				request.getIncludeInResultAttributes(), 
				context.getEvaluatedPolicies());
	}
}
