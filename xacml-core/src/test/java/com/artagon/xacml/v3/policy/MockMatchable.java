package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Matchable;
import com.artagon.xacml.v3.policy.PolicyVisitor;

class MockMatchable implements Matchable
{
	private MatchResult result;
	
	MockMatchable(MatchResult r){
		this.result = r;
	}

	@Override
	public MatchResult match(EvaluationContext context) {
		return result;
	}

	@Override
	public void accept(PolicyVisitor v) {
		// TODO Auto-generated method stub
		
	}
	
	
}
