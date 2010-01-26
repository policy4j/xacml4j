package com.artagon.xacml.policy;

import java.util.Collections;
import java.util.List;

import com.artagon.xacml.DecisionResult;

public class MockCompositeDecision extends BaseCompositeDecision 
{
	private DecisionResult d;
	
	public MockCompositeDecision(DecisionResult d, MatchResult m) {
		super("MockCompisiteDecision", new MockTarget(m), 
				Collections.<AdviceExpression>emptyList(),
				Collections.<ObligationExpression>emptyList());
		this.d = d;
	}

	@Override
	public List<? extends Decision> getDecisions() {
		return Collections.emptyList();
	}

	@Override
	protected DecisionResult doEvaluate(EvaluationContext context) {
		return d;
	}
	
	@Override
	public EvaluationContext createContext(EvaluationContext context) {
		return context;
	}
	
	@Override
	protected boolean isEvaluationContextValid(EvaluationContext context) {
		return true;
	}

	@Override
	public void accept(PolicyVisitor v) {
		// TODO Auto-generated method stub
		
	}	
	
	
}
