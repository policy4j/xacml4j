package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Decision;

public class MockDecisionRule extends BaseDesicionRule
{
	private Decision decision;
	
	public MockDecisionRule(Decision decision){
		this(decision, MatchResult.MATCH);
	}
	

	public MockDecisionRule(Decision decision, MatchResult match){
		super("MockDecisionId", new MockTarget(match));
		this.decision = decision;
	}
	
	@Override
	public EvaluationContext createContext(EvaluationContext parentContext) {
		return parentContext;
	}

	protected Decision doEvaluate(EvaluationContext context) {
		return decision;
	}
	
	

	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context) {
		return true;
	}


	@Override
	public void accept(PolicyVisitor v) {
		
	}	
}
