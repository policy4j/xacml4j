package com.artagon.xacml.v3.policy.combine.legacy;

import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class DenyOverridesPolicyCombineAlgorithm extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides";

	public DenyOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
	
	protected DenyOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	@Override
	public Decision combine(List<CompositeDecisionRule> rules,
			EvaluationContext context) {
		boolean atLeastOnePermit = false;
		for(CompositeDecisionRule r : rules){
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
