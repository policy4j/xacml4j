package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class LegacyDenyOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm<Rule> 
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides";

	public LegacyDenyOverridesRuleCombineAlgorithm() {
		super(ID);
	}
	
	LegacyDenyOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public Decision combine(List<Rule> rules, EvaluationContext context) 
	{
		boolean potentialDeny	= false;
		boolean atLeastOnePermit = false;
		boolean atLeastOneError = false;
		for(Rule r : rules)
		{
			Decision d = evaluateIfApplicable(context, r);
		
			if (d == Decision.DENY){
				return Decision.DENY;
			}
			if(d == Decision.PERMIT){
				atLeastOnePermit = true;
				continue;
			}
			if (d == Decision.NOT_APPLICABLE){
				continue;
			}
			if (d.isIndeterminate()){
				atLeastOneError = true;
				if (r.getEffect() == Effect.DENY){
					potentialDeny = true;
				}
				continue;
			}
		}
		if(potentialDeny){
			return Decision.INDETERMINATE;
		}
		if(atLeastOnePermit){
			return Decision.PERMIT;
		}
		if(atLeastOneError){
			return Decision.INDETERMINATE;
		}
		return Decision.NOT_APPLICABLE;
	}
}
