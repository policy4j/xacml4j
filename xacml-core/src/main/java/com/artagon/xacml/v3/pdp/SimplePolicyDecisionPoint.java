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
import com.artagon.xacml.v3.profiles.RequestProfileHandler;
import com.google.common.base.Preconditions;

public class SimplePolicyDecisionPoint implements PolicyDecisionPoint 
{
	private EvaluationContextFactory factory;
	private CompositeDecisionRule policySet;
	
	public SimplePolicyDecisionPoint(
			List<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			CompositeDecisionRule policySet)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policySet);
		this.factory = factory;
		this.policySet = policySet;
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
		EvaluationContext context = factory.createContext(policySet, request);
		Decision decision = policySet.evaluateIfApplicable(context);
		if(decision.isIndeterminate() || 
				decision == Decision.NOT_APPLICABLE){
			return new Response(
					new Result(decision, 
							new Status(decision.isIndeterminate()?StatusCode.createProcessingError():StatusCode.createOk()),
							request.getIncludeInResultAttributes(), 
							context.getEvaluatedPolicies()));
		}
		return new Response(
				new Result(
						decision, 
						new Status(StatusCode.createOk()),
						context.getAdvices(), 
						context.getObligations(), 
						request.getIncludeInResultAttributes(), 
						context.getEvaluatedPolicies()));
	}
}
