package com.artagon.xacml.v3.policy.combine.legacy;

import java.util.List;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class PermitOverridesPolicyCombineAlgorithm extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides";

	protected PermitOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	public PermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}

	@Override
	public final Decision combine(List<CompositeDecisionRule> rules,
			EvaluationContext context) 
	{
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		for(CompositeDecisionRule r : rules){
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
