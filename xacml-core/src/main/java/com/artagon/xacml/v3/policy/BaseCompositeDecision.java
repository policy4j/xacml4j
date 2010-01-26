package com.artagon.xacml.v3.policy;

import java.util.Collection;

abstract class BaseCompositeDecision extends BaseDesicion 
	implements CompositeDecision
{
	
	protected BaseCompositeDecision(
			String id, Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(id, target, adviceExpressions, obligationExpressions);
	}
}
