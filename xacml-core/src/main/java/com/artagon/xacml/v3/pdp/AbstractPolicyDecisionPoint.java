package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.profiles.RequestProfileHandler;
import com.google.common.base.Preconditions;

public abstract class AbstractPolicyDecisionPoint implements PolicyDecisionPoint 
{
	private EvaluationContextFactory factory;
	private PolicySet policySet;
	
	
	protected AbstractPolicyDecisionPoint(
			Collection<RequestProfileHandler> handlers,
			EvaluationContextFactory factory,  
			PolicySet policySet)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policySet);
		this.factory = factory;
		this.policySet = policySet;
	}

	@Override
	public Response decide(Request request)
	{
		EvaluationContext context = factory.createContext(policySet, request);
		Decision decision = policySet.evaluateIfApplicable(context);
		Result result = new Result(decision, 
				context.getAdvices(), 
				context.getObligations(), 
				request.getIncludeInResultAttributes(), 
				context.getEvaluatedPolicies());
		return new Response(Collections.singleton(result));
	}
}
