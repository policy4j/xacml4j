package com.artagon.xacml.v30.policy.combine;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.pdp.Rule;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;

public class LegacyPermitOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm<Rule> 
{
	private final static String ID ="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";

	
	protected LegacyPermitOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	public LegacyPermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}
	
	
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides")
	@Override
	public Decision combine(EvaluationContext context, List<Rule> rules) 
	{
		boolean atLeastOneError = false;
		boolean potentialPermit = false;
		boolean atLeastOneDeny = false;
		for(Rule r : rules)
		{
			Decision d = evaluateIfMatch(context, r);
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
