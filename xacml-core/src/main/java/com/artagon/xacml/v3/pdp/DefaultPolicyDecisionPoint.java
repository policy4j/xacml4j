package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Attributes;
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
import com.artagon.xacml.v3.spi.PolicyStore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class DefaultPolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDecisionPoint.class);
	
	private EvaluationContextFactory factory;
	private PolicyStore policyRepository;
	private RequestProfileHandlerChain requestProcessingPipeline;
	
	public DefaultPolicyDecisionPoint(
			List<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			PolicyStore policyRepository)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policyRepository);
		this.factory = factory;
		this.policyRepository = policyRepository;
		this.requestProcessingPipeline = new RequestProfileHandlerChain(handlers);
	}
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory,  
			PolicyStore policyRepostory)
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
		Collection<Attributes> includeInResult = request.getIncludeInResultAttributes();
		if(applicable.size() == 0){
			return new Result(Decision.NOT_APPLICABLE, 
					new Status(StatusCode.createOk(), 
							"No applicable policies found"), includeInResult);
		}
		if(applicable.size() > 1){
			return new Result(Decision.INDETERMINATE, 
					new Status(StatusCode.createProcessingError(), 
							"Found more than one applicable policy"), 
							includeInResult);
		}
		CompositeDecisionRule policy = Iterables.getOnlyElement(applicable);
		EvaluationContext policyContext = policy.createContext(context);
		Decision decision = policy.evaluateIfApplicable(policyContext);
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					new Status(StatusCode.createOk()), includeInResult);
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, new Status(status), includeInResult);
		}
		return new Result(
				decision, 
				new Status(StatusCode.createOk()),
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				context.getEvaluatedPolicies());
	}
}
