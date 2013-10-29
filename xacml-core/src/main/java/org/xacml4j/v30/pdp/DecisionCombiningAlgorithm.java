package org.xacml4j.v30.pdp;


import java.util.List;

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;


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
