package com.artagon.xacml.v3;


public interface ObligationExpression extends DecisionRuleResponseExpression
{
	Obligation evaluate(EvaluationContext context) throws EvaluationException;
}