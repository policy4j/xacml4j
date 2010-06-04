package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.context.ContextFactory;
import com.artagon.xacml.v3.policy.PolicySet;
import com.google.common.base.Preconditions;

public abstract class AbstractPolicyDecisionPoint implements PolicyDecisionPoint 
{
	private EvaluationContextFactory factory;
	private PolicySet policySet;
	private ContextFactory contextFactory;
	
	protected AbstractPolicyDecisionPoint(
			EvaluationContextFactory factory, ContextFactory contextFactory, PolicySet policySet)
	{
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policySet);
		Preconditions.checkNotNull(contextFactory);
		this.factory = factory;
		this.policySet = policySet;
		this.contextFactory = contextFactory;
	}

//	@Override
//	public Response decide(Request request)
//	{
//		EvaluationContext context = factory.createContext(policySet, request);
//		Decision decision = policySet.evaluateIfApplicable(context);
//		Collection<Attributes> responseAttributes = request.getIncludeInResultAttributes();
//		Result result = contextFactory.createResult(decision, 
//				context.getAdvices(), 
//				context.getObligations(), 
//				request.getIncludeInResultAttributes(), 
//				Collections.<PolicyIdentifier>emptyList());
//		return contextFactory.createResponse(Collections.singleton(result));
//	}
}
