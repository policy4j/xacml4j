package com.artagon.xacml.policy;

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
