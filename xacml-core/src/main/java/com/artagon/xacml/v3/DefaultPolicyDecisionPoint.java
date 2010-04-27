package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.PolicySet;

public final class DefaultPolicyDecisionPoint extends AbstractPolicyDecisionPoint
{
	private EvaluationContextFactory factory;
	private PolicySet policySet;
	
	public DefaultPolicyDecisionPoint(EvaluationContextFactory factory, 
			PolicySet policySet, 
			Collection<RequestProfileHandler> handlers){
		super(handlers);
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(policySet);
		this.factory = factory;
		this.policySet = policySet;
	}

	@Override
	protected Result doDecide(Request request) 
	{
		EvaluationContext context = factory.createContext(policySet, request);
		Decision decision = policySet.evaluate(context);
		Collection<Attributes> includeInResult = request.getIncludeInResultAttributes();
		Collection<PolicyIdentifier> policyIdentifiers = Collections.emptyList();
		return new Result(decision, 
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				policyIdentifiers);
	}
}
