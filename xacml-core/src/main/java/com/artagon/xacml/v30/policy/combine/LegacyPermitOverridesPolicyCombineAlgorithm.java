package com.artagon.xacml.v30.policy.combine;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;

public class LegacyPermitOverridesPolicyCombineAlgorithm
	extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides";

	protected LegacyPermitOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}

	public LegacyPermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}

	@Override
	public final Decision combine(EvaluationContext context,
			List<CompositeDecisionRule> rules){
		return doCombine(context, rules);
	}

	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides")
	public final static  Decision doCombine(EvaluationContext context,
			List<CompositeDecisionRule> rules)
	{
		boolean atLeastOneError = false;
		boolean atLeastOneDeny = false;
		for(CompositeDecisionRule r : rules){
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
