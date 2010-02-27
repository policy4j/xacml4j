package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionRuleResponse;

public interface DecisionRuleResponseExpression extends PolicyElement
{
	/**
	 * Unique identifier
	 * 
	 * @return an identifier
	 */
	String getId();
	
	/**
	 * Gets {@link Effect} instance
	 * 
	 * @return {@link Effect} instance
	 */
	Effect getEffect();
	
	/**
	 * Tests if this decision info expression
	 * is applicable for a given {@link Decision}
	 * 
	 * @param result a decision result
	 * @return <code>true</code> if an expression is applicable
	 */
	boolean isApplicable(Decision result);
	
	/**
	 * Evaluates this decision rule respons expression
	 * 
	 * @param context an evaluation context
	 * @return {@link DecisionRuleResponse} as an evaluation result
	 * @throws EvaluationException if an evaluation error occurs
	 */
	DecisionRuleResponse evaluate(EvaluationContext context) 
		throws EvaluationException;
}
