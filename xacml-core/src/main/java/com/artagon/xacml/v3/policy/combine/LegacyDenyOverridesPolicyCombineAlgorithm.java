package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.ReferencableDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class LegacyDenyOverridesPolicyCombineAlgorithm extends BaseDecisionCombiningAlgorithm<ReferencableDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides";

	public LegacyDenyOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
	
	protected LegacyDenyOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	@Override
	public Decision combine(List<ReferencableDecisionRule> rules,
			EvaluationContext context) {
		boolean atLeastOnePermit = false;
		for(ReferencableDecisionRule r : rules){
			Decision d = evaluateIfApplicable(context, r);
			if(d == Decision.DENY){
				return d;
			}
			if(d == Decision.PERMIT){
				atLeastOnePermit = true;
				continue;
			}
			if(d == Decision.NOT_APPLICABLE){
				continue;
			}
			if(d.isIndeterminate()){
				return Decision.DENY;
			}
		}
		if(atLeastOnePermit){
			return Decision.PERMIT;
		}
		return Decision.NOT_APPLICABLE;
	}	
}
