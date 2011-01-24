package com.artagon.xacml.v30;


import java.util.List;

public interface DecisionCombiningAlgorithm <D extends DecisionRule> 
{
	/**
	 * Gets algorithm identifier
	 * 
	 * @return algorithm identifier
	 */
	String getId();
	
	/**
	 * Combines multiple decisions to one {@link Decision} result
	 * 
	 * @param decisions a multiple decisions
	 * @param context an evaluation context
	 * @return {@link Decision} context
	 */
	Decision combine(List<D> decisions, EvaluationContext context);
}
