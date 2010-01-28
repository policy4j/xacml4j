package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.MatchResult;

public class PolicyOnlyOneApplicable extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule> 
{
	private final static Logger log = LoggerFactory.getLogger(PolicyOnlyOneApplicable.class);
	
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";
	
	public PolicyOnlyOneApplicable() {
		super(ID);
	}

	@Override
	public DecisionResult combine(List<CompositeDecisionRule> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		EvaluationContext policyContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			policyContext = d.createContext(context);
			MatchResult r = d.isApplicable(policyContext);
			log.debug("Decision id=\"{}\" applicability=\"{}\"", d.getId(), r);
			if(r == MatchResult.INDETERMINATE){
				return DecisionResult.INDETERMINATE;
			}
			if(r == MatchResult.MATCH){
				if(atLeastOne){
					return DecisionResult.INDETERMINATE;
				}
				atLeastOne = true;
				found = d;
			}
			continue;
		}
		if(atLeastOne && log.isDebugEnabled()){
			log.debug("Found one applicable decision id=\"{}\"", found.getId());
		}
		return atLeastOne?found.evaluate(policyContext):DecisionResult.NOT_APPLICABLE;
	}
}
