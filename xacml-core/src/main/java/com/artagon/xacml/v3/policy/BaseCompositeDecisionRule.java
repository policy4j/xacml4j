package com.artagon.xacml.v3.policy;

import java.util.Collection;

abstract class BaseCompositeDecisionRule extends BaseDesicionRule 
	implements CompositeDecisionRule
{
	
	protected BaseCompositeDecisionRule(
			String id, Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(id, target, adviceExpressions, obligationExpressions);
	}
}
