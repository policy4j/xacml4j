package com.artagon.xacml.v3.policy.spi;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.Rule;

public interface DecisionCombingingAlgorithmProvider 
{
	boolean isRuleAgorithmProvided(String algorithmId);
	boolean isPolicyAgorithmProvided(String algorithmId);
	Iterable<DecisionCombiningAlgorithm<Rule>> getRuleAlgorithms();
	Iterable<DecisionCombiningAlgorithm<? extends CompositeDecisionRule>> getPolicyAlgorithms();
}