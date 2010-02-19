package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Obligation;

public interface ObligationExpression extends DecisionRuleResponseExpression
{
	Obligation evaluate(EvaluationContext context) throws EvaluationException;
}