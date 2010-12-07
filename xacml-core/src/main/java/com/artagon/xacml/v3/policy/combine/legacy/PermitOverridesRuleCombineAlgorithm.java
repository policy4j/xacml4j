package com.artagon.xacml.v3.policy.combine.legacy;

import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class PermitOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm<Rule> 
{
	private final static String ID ="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";

	
	protected PermitOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	public PermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}


	@Override
	public Decision combine(List<Rule> rules, EvaluationContext context) 
	{
		boolean atLeastOneError = false;
		boolean potentialPermit = false;
		boolean atLeastOneDeny = false;
		for(Rule r : rules)
		{
			Decision d = evaluateIfApplicable(context, r);
			if(d == Decision.DENY){
				atLeastOneDeny = true;
				continue;
			}
			if(d == Decision.PERMIT){
				return Decision.PERMIT;
			}
			if(d == Decision.NOT_APPLICABLE){
				continue;
			}
			if(d.isIndeterminate()){
				atLeastOneError = true;
				if(r.getEffect() == Effect.PERMIT){
					potentialPermit = true;
				}
				continue;
			}
		}
		if(potentialPermit){
			return Decision.INDETERMINATE;
		}
		if(atLeastOneDeny){
			return Decision.DENY;
		}
		if(atLeastOneError){
			return Decision.INDETERMINATE;
		}
		return Decision.NOT_APPLICABLE;
	}
}
