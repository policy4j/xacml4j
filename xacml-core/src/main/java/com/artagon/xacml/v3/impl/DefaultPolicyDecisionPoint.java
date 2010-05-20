package com.artagon.xacml.v3.impl;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.PolicyIdentifier;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProfileHandler;
import com.artagon.xacml.v3.Result;
import com.google.common.base.Preconditions;

public final class DefaultPolicyDecisionPoint extends AbstractPolicyDecisionPoint
{
	private EvaluationContextFactory factory;
	private PolicySet policySet;
	
	public DefaultPolicyDecisionPoint(
			EvaluationContextFactory factory, 
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
		EvaluationContext policySetContext = policySet.createContext(context);
		Decision decision = policySet.evaluateIfApplicable(policySetContext);
		Iterable<Attributes> includeInResult = request.getIncludeInResultAttributes();
		return new Result(
				decision, 
				context.getAdvices(), 
				context.getObligations(), 
				includeInResult, 
				(request.isReturnPolicyIdList()?
						context.getEvaluatedPolicies():Collections.<PolicyIdentifier>emptyList()));
	}	
}
