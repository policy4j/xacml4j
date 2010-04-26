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
	private PolicySet rootPolicySet;
	
	public DefaultPolicyDecisionPoint(EvaluationContextFactory factory, 
			PolicySet rootPolicySet, Collection<RequestProfileHandler> handlers){
		super(handlers);
		Preconditions.checkNotNull(factory);
		Preconditions.checkNotNull(rootPolicySet);
		this.factory = factory;
		this.rootPolicySet = rootPolicySet;
	}

	@Override
	protected Result doDecide(Request request) 
	{
		EvaluationContext context = factory.createContext(rootPolicySet, request);
		Decision decision = rootPolicySet.evaluate(context);
		Collection<Attributes> includeInResponse = Collections.emptyList();
		Collection<PolicyIdentifier> policyIdentifiers = Collections.emptyList();
		return new Result(decision, 
				context.getAdvices(), 
				context.getObligations(), 
				includeInResponse, 
				policyIdentifiers);
	}
}
