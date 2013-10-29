package org.xacml4j.v30.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;


public class LegacyDenyOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm<Rule>
{
	private final static Logger log = LoggerFactory.getLogger(LegacyDenyOverridesRuleCombineAlgorithm.class);

	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides";

	public LegacyDenyOverridesRuleCombineAlgorithm() {
		super(ID);
	}

	LegacyDenyOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public Decision combine(EvaluationContext context, List<Rule> rules){
		return doCombine(context, rules);
	}

	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides")
	public static Decision doCombineOrdered(EvaluationContext context, List<Rule> rules){
		return doCombine(context, rules);
	}

	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides")
	public static Decision doCombine(EvaluationContext context, List<Rule> rules)
	{
		boolean potentialDeny	= false;
		boolean atLeastOnePermit = false;
		boolean atLeastOneError = false;
		for(Rule r : rules)
		{
			Decision d = r.evaluate(r.createContext(context));
			if(log.isDebugEnabled()){
				log.debug("Decision rule id=\"{}\" evaluation result=\"{}\"", r.getId(), d);
			}
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
