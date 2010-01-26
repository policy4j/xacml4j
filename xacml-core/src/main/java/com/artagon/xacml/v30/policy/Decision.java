package com.artagon.xacml.v30.policy;

import java.util.Collection;

import com.artagon.xacml.v30.DecisionResult;

public interface Decision extends PolicyElement
{
	/**
	 * Gets decision identifier
	 * 
	 * @return decision identifier
	 */
	String getId();
	
	/**
	 * Gets decision target
	 * 
	 * @return {@link DefaultTarget} or
	 * <code>null</code> if decision
	 * matches any request
	 */
	Target getTarget();
	
	/**
	 * Creates an evaluation context to match 
	 * or evaluate this decision
	 * 
	 * @param context a parent evaluation context
	 * @return {@link EvaluationContext} instance to match or evaluate
	 * this decision 
	 */
	EvaluationContext createContext(EvaluationContext context);
	
	/**
	 * Gets decision obligations
	 * 
	 * @return collection of {@link ObligationExpression}
	 * instances
	 */
	Collection<ObligationExpression> getObligationExpressions();
	
	/**
	 * Gets decision advice expressions
	 * 
	 * @return collection of {@link AdviceExpression}
	 * instances
	 */
	Collection<AdviceExpression> getAdviceExpressions();
	
	/**
	 * Combines {@link #isApplicable(EvaluationContext)} with
	 * an actual decision evaluation to one invocation.
	 * 
	 * @param context an evaluation context for this decision
	 * @return {@link DecisionResult}
	 */
	DecisionResult evaluateIfApplicable(EvaluationContext context);
	
	/**
	 * Tests if decision is applicable to a given
	 * evaluation context. An evaluation context
	 * must be created by invoking 
	 * {@link this#createContext(EvaluationContext)} first
	 * 
	 * @param context an evaluation context
	 * @return {@link MatchResult} indicating applicability
	 * of this decision to the given evaluation context
	 */
	MatchResult isApplicable(EvaluationContext context);
	
	/**
	 * Evaluates this decision in the given evaluation context.
	 * An evaluation context must be created by invoking 
	 * {@link this#createContext(EvaluationContext)} first.
	 * Evaluation should be performed if prior call
	 * to {@link #isApplicable(EvaluationContext)} returns
	 * {@link MatchResult#MATCH}
	 * 
	 * @param context an evaluation context
	 * @return {@link DecisionResult}
	 */
	DecisionResult evaluate(EvaluationContext context);
}
