package org.xacml4j.v30.policy.combine;

import java.util.List;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;


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
