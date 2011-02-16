package com.artagon.xacml.v30.policy.combine;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfApplicable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;

public class FirstApplicable<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	private final static Logger log = LoggerFactory.getLogger(FirstApplicable.class);
	
	protected FirstApplicable(String algorithmId) {
		super(algorithmId);
	}

	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable")
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")
	@Override
	public final Decision combine(List<D> decisions,
			EvaluationContext context) 
	{
		for(D d : decisions){
			Decision decision = evaluateIfApplicable(context, d);
			if(log.isDebugEnabled()){
				log.debug("Decision=\"{}\" " +
						"evaluation result=\"{}\"", d, decision);
			}
			if(decision == Decision.DENY){
				return decision;
			}
			if(decision == Decision.PERMIT){
				return decision;
			}
			if(decision == Decision.NOT_APPLICABLE){
				continue;
			}
			if(decision.isIndeterminate()){
				return decision;
			}
		}
		return Decision.NOT_APPLICABLE;
	}
}
