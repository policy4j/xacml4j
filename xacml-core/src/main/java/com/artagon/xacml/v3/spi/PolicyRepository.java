package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.EvaluationContext;

public interface PolicyRepository extends PolicyReferenceResolver
{
	/**
	 * Gets top level policies
	 * 
	 * @return a collection of {@link CompositeDecisionRule} instances
	 */
	Collection<CompositeDecisionRule> getPolicies();
	
	/**
	 * Finds all applicable top level policies
	 * 
	 * @param context an evaluation context
	 * @return a collection of {@link CompositeDecisionRule} instances
	 */
	Collection<CompositeDecisionRule> findApplicable(EvaluationContext context);
}
