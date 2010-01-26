package com.artagon.xacml.v30.policy;

import java.util.Collections;
import java.util.List;

import com.artagon.xacml.v30.DecisionResult;
import com.artagon.xacml.v30.policy.AdviceExpression;
import com.artagon.xacml.v30.policy.BaseCompositeDecision;
import com.artagon.xacml.v30.policy.Decision;
import com.artagon.xacml.v30.policy.EvaluationContext;
import com.artagon.xacml.v30.policy.MatchResult;
import com.artagon.xacml.v30.policy.ObligationExpression;
import com.artagon.xacml.v30.policy.PolicyVisitor;

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
