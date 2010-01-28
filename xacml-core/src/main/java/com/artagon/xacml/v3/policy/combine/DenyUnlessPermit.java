package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;

public class DenyUnlessPermit<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected DenyUnlessPermit(String algorithmId){
		super(algorithmId);
	}
	
	public DecisionResult combine(List<D> decisions,
			EvaluationContext context)
	{
		for(D d : decisions){
			DecisionResult decision = evaluateIfApplicable(context, d);
			if(decision == DecisionResult.PERMIT){
				return decision;
			}
		}
		return DecisionResult.DENY;
	}
	
}
