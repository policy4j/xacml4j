package com.artagon.xacml.v30.policy.combine;

import java.util.List;

import com.artagon.xacml.v30.pdp.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.pdp.MatchResult;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;

public final class OnlyOneApplicablePolicyCombingingAlgorithm extends 
	BaseDecisionCombiningAlgorithm<CompositeDecisionRule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";
	
	public OnlyOneApplicablePolicyCombingingAlgorithm() {
		super(ID);
	}

	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable")
	@Override
	public Decision combine(EvaluationContext context, List<CompositeDecisionRule> decisions) 
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		EvaluationContext foundEvalContext = null;
		EvaluationContext policyContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			policyContext = d.createContext(context);
			MatchResult r = d.isMatch(policyContext);
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
