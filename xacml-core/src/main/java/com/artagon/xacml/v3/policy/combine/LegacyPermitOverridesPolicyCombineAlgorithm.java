package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.ReferencableDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class LegacyPermitOverridesPolicyCombineAlgorithm extends BaseDecisionCombiningAlgorithm<ReferencableDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides";

	protected LegacyPermitOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	public LegacyPermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}

	@Override
	public final Decision combine(List<ReferencableDecisionRule> rules,
			EvaluationContext context) 
	{
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		for(ReferencableDecisionRule r : rules){
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
			if(d.isIndeterminate())
			{
				atLeastOneError = true;
				continue;
			}
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
