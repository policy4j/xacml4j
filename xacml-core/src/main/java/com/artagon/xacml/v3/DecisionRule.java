package com.artagon.xacml.v3;

/**
 * A XACML access decision rule, core interface
 * for all rules in the XACML specification
 * 
 * @author Giedrius Trumpickas
 */
public interface DecisionRule extends PolicyElement
{
	/**
	 * Gets decision identifier
	 * 
	 * @return decision identifier
	 */
	String getId();
	
	/**
	 * Creates an evaluation context to match 
	 * or evaluate this decision rule
	 * 
	 * @param context a parent evaluation context
	 * @return {@link EvaluationContext} an evaluation
	 * context to be used to match or evaluate this decision
	 */
	EvaluationContext createContext(EvaluationContext context);
		
	/**
	 * Combines {@link #isApplicable(EvaluationContext)} with
	 * an actual decision rule evaluation to one invocation.
	 * 
	 * @param context an evaluation context for this decision
	 * @return {@link Decision}
	 */
	Decision evaluateIfApplicable(EvaluationContext context);
	
	/**
	 * Tests if this decision rule is applicable to a given
	 * evaluation context. An evaluation context
	 * must be created by invoking 
	 * {@link this#createContext(EvaluationContext)} first
	 * 
	 * @param context an evaluation context
	 * @return {@link MatchResult} indicating applicability
	 * of this decision rule to the given evaluation context
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
	 * @return {@link Decision}
	 */
	Decision evaluate(EvaluationContext context);
}
