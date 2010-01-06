package com.artagon.xacml.policy;

public class MockDecision extends BaseDesicion
{
	private DecisionResult decision;
	
	public MockDecision(DecisionResult decision){
		this(decision, MatchResult.MATCH);
	}
	

	public MockDecision(DecisionResult decision, MatchResult match){
		super("MockDecisionId", new MockTarget(match));
		this.decision = decision;
	}
	
	@Override
	public EvaluationContext createContext(EvaluationContext parentContext) {
		return parentContext;
	}

	protected DecisionResult doEvaluate(EvaluationContext context) {
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
