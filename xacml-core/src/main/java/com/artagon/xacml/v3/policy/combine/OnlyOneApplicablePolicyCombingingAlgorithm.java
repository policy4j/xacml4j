package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

final class OnlyOneApplicablePolicyCombingingAlgorithm extends 
	BaseDecisionCombiningAlgorithm<CompositeDecisionRule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";
	
	public OnlyOneApplicablePolicyCombingingAlgorithm() {
		super(ID);
	}

	@Override
	public Decision combine(List<CompositeDecisionRule> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		EvaluationContext foundEvalContext = null;
		EvaluationContext policyContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			policyContext = d.createContext(context);
			MatchResult r = d.isApplicable(policyContext);
			if(r == MatchResult.INDETERMINATE){
				return Decision.INDETERMINATE;
			}
			if(r == MatchResult.MATCH){
				if(atLeastOne){
					return Decision.INDETERMINATE;
				}
				atLeastOne = true;
				found = d;
				foundEvalContext = policyContext;
			}
			continue;
		}
		return atLeastOne?found.evaluate(foundEvalContext):Decision.NOT_APPLICABLE;
	}
}
