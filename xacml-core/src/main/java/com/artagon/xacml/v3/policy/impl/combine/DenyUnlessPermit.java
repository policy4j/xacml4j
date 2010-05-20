package com.artagon.xacml.v3.policy.impl.combine;

import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombiningAlgorithm;

public class DenyUnlessPermit<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected DenyUnlessPermit(String algorithmId){
		super(algorithmId);
	}
	
	public Decision combine(List<D> decisions,
			EvaluationContext context)
	{
		for(D d : decisions){
			Decision decision = evaluateIfApplicable(context, d);
			if(decision == Decision.PERMIT){
				return decision;
			}
		}
		return Decision.DENY;
	}
	
}
