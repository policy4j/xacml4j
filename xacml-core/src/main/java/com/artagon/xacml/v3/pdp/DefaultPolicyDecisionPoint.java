package com.artagon.xacml.v3.pdp;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.profiles.RequestProfileHandlerChain;
import com.artagon.xacml.v3.profiles.RequestProfileHandler;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.google.common.base.Preconditions;

public class DefaultPolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
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
		Map<String, CompositeDecisionRule> applicable = policyRepository.findApplicable(context);
		CompositeDecisionRule policy = resolver.resolve(applicable);
		if(policy  == null){
			return new Result(Decision.NOT_APPLICABLE, 
					new Status(StatusCode.createOk()));
		}
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
