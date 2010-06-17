package com.artagon.xacml.v3.pdp;

import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.Status;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.profiles.CompositeRequestProfileHandler;
import com.artagon.xacml.v3.profiles.RequestProfileHandler;
import com.google.common.base.Preconditions;

public class SimplePolicyDecisionPoint implements PolicyDecisionPoint, 
	PolicyDecisionCallback
{
	private EvaluationContextFactory factory;
	private CompositeDecisionRule policySet;
	private CompositeRequestProfileHandler handler;
	
	public SimplePolicyDecisionPoint(
			List<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			CompositeDecisionRule policySet)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policySet);
		this.factory = factory;
		this.policySet = policySet;
		this.handler = new CompositeRequestProfileHandler(handlers);
	}
	
	public SimplePolicyDecisionPoint(
			EvaluationContextFactory factory,  
			CompositeDecisionRule policySet)
	{
		this(Collections.<RequestProfileHandler>emptyList(), factory, policySet);
	}

	@Override
	public Response decide(Request request)
	{
		return new Response(handler.handle(request, this));			
	}

	@Override
	public Result requestDecision(Request request) 
	{
		EvaluationContext context = factory.createContext(policySet, request);
		Decision decision = policySet.evaluateIfApplicable(context);
		if(decision == Decision.NOT_APPLICABLE){
			return new Result(decision, 
					new Status(StatusCode.createOk()),
					request.getIncludeInResultAttributes(), 
					context.getEvaluatedPolicies());
		}
		if(decision.isIndeterminate()){
			StatusCode status = (context.getEvaluationStatus() == null)?
					StatusCode.createProcessingError():context.getEvaluationStatus();
			return new Result(decision, 
							new Status(status),
							request.getIncludeInResultAttributes(), 
							context.getEvaluatedPolicies());
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
