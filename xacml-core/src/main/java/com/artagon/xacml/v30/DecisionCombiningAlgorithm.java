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
	 * @param context an evaluation context
	 * @param decisions a multiple decisions
	 * @return {@link Decision} context
	 */
	Decision combine(EvaluationContext context, List<D> decisions);
}
