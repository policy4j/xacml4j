package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.Target;

public class MockTarget implements Target
{
	private MatchResult m;
	
	public MockTarget(MatchResult m){
		this.m = m;
	}

	@Override
	public MatchResult match(EvaluationContext context) {
		return m;
	}

	@Override
	public void accept(PolicyVisitor v) {
		// TODO Auto-generated method stub
		
	}
	
	
}
