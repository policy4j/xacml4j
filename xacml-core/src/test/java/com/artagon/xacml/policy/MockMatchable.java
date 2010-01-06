package com.artagon.xacml.policy;

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
