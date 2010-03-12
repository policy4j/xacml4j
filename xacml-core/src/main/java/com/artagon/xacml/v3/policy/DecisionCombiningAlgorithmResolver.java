package com.artagon.xacml.v3.policy;

import java.util.Set;


public interface DecisionCombiningAlgorithmResolver 
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
	 * Gets {@link CompositeDecisionRule} combining algorithm via
	 * given algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithm} for combining
	 * policy or policy set decision results
	 */
	DecisionCombiningAlgorithm<? extends CompositeDecisionRule> getPolicyAlgorithm(String algorithmId);
	
	Set<String> getSupprtedRuleAlgorithms();
	Set<String> getSupprtedPolicyAlgorithms();
}
