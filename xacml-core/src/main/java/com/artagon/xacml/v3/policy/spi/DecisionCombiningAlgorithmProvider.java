package com.artagon.xacml.v3.policy.spi;

import java.util.Set;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Rule;

public interface DecisionCombiningAlgorithmProvider 
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
	
	Set<String> getSupportedRuleAlgorithms();
	Set<String> getSupportedPolicyAlgorithms();
	
	boolean isRuleAgorithmProvided(String algorithmId);
	boolean isPolicyAgorithmProvided(String algorithmId);
}