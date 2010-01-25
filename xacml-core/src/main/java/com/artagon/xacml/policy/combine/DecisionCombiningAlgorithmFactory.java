package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.policy.Policy;
import com.artagon.xacml.policy.Rule;

public interface DecisionCombiningAlgorithmFactory 
{
	/**
	 * Gets {@link Rule} combining algorithm via
	 * given algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithm} for combining
	 * rule decision results
	 */
	DecisionCombiningAlgorithm<Rule> getRuleAlgorithm(String algorithmId);
	
	/**
	 * Gets {@link Policy} combining algorithm via
	 * given algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithm} for combining
	 * policy or policy set decision results
	 */
	DecisionCombiningAlgorithm<Policy> getPolicyAlgorithm(String algorithmId);
}
