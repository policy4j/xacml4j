package com.artagon.xacml.v3;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.PolicyElement;
import com.artagon.xacml.v3.policy.ValueType;

public interface Expression  extends PolicyElement
{	
	/**
	 * Gets type to which this expression
	 * evaluates to
	 * 
	 * @return {@link ValueType}
	 */
	ValueType getEvaluatesTo();
	
	/**
	 * Evaluates this expression
	 * 
	 * @param context an evaluation context
	 * @return {@link Expression} an expression 
	 * representing evaluation result
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	Expression evaluate(EvaluationContext context) throws EvaluationException;
}
