package org.xacml4j.v30.policy.combine;

import static org.xacml4j.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;


public class LegacyDenyOverridesPolicyCombineAlgorithm extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides";

	public LegacyDenyOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
	
	protected LegacyDenyOverridesPolicyCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}
	
	public Decision combine(EvaluationContext context, 
			List<CompositeDecisionRule> rules) {
		return doCombine(context, rules);
	}
	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides")
	public static <D extends CompositeDecisionRule> Decision doCombine(EvaluationContext context, 
			List<D> rules) {
		boolean atLeastOnePermit = false;
		for(CompositeDecisionRule r : rules){
			Decision d = evaluateIfMatch(context, r);
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
