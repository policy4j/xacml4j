package com.artagon.xacml.v30.policy;

import com.artagon.xacml.v30.policy.EvaluationContext;
import com.artagon.xacml.v30.policy.MatchResult;
import com.artagon.xacml.v30.policy.PolicyVisitor;
import com.artagon.xacml.v30.policy.Target;

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
