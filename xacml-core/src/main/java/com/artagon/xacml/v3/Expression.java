package com.artagon.xacml.v3;

import com.artagon.xacml.v3.policy.PolicyElement;

/**
 * A XACML policy expression
 * 
 * @author Giedrius Trumpickas
 */
public interface Expression extends PolicyElement
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
	 * representing evaluation result, usually evaluation result
	 * is an instance {@link ValueExpression} but in some cases
	 * expression evaluates to itself
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	Expression evaluate(EvaluationContext context) throws EvaluationException;
}
