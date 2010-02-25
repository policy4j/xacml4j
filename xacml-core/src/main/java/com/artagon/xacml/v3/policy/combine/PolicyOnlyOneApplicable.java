package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.MatchResult;

public class PolicyOnlyOneApplicable extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule> 
{
	private final static Logger log = LoggerFactory.getLogger(PolicyOnlyOneApplicable.class);
	
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";
	
	public PolicyOnlyOneApplicable() {
		super(ID);
	}

	@Override
	public Decision combine(List<CompositeDecisionRule> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		EvaluationContext policyContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			try{
				policyContext = d.createContext(context);
			}catch(EvaluationException e){
				return Decision.INDETERMINATE;
			}
			MatchResult r = d.isApplicable(policyContext);
			if(r == MatchResult.INDETERMINATE){
				return Decision.INDETERMINATE;
			}
			if(r == MatchResult.MATCH){
				if(atLeastOne){
					return Decision.INDETERMINATE;
				}
				atLeastOne = true;
				found = d;
			}
			continue;
		}
		return atLeastOne?found.evaluate(policyContext):Decision.NOT_APPLICABLE;
	}
}
