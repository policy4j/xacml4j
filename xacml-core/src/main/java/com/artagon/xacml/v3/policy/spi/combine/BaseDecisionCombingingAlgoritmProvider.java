package com.artagon.xacml.v3.policy.spi.combine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.policy.spi.DecisionCombingingAlgorithmProvider;
import com.google.common.base.Preconditions;

public class BaseDecisionCombingingAlgoritmProvider implements DecisionCombingingAlgorithmProvider
{
	private Map<String, DecisionCombiningAlgorithm<Rule>> ruleAlgo;
	private Map<String, DecisionCombiningAlgorithm<? extends CompositeDecisionRule>> policyAlgo;
	
	protected BaseDecisionCombingingAlgoritmProvider(){
		this.ruleAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<Rule>>();
		this.policyAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<? extends CompositeDecisionRule>>();
	}
	
	public final boolean isRuleAgorithmProvided(String algorithmId){
		return ruleAlgo.containsKey(algorithmId);
	}
	
	public final boolean isPolicyAgorithmProvided(String algorithmId){
		return policyAlgo.containsKey(algorithmId);
	}
	
	public final void addRuleCombineAlgorithm(
			DecisionCombiningAlgorithm<Rule> algorithm)
	{
		DecisionCombiningAlgorithm<Rule> oldAlgo = ruleAlgo.put(algorithm.getId(), algorithm);
		Preconditions.checkState(oldAlgo != null);
	}
	
	public final void addCompositeRuleCombineAlgorithm(
			DecisionCombiningAlgorithm<? extends CompositeDecisionRule> algorithm)
	{
		DecisionCombiningAlgorithm<? extends CompositeDecisionRule> oldAlgo = policyAlgo.put(
				algorithm.getId(), algorithm);
		Preconditions.checkState(oldAlgo != null);
	}
	
	@Override
	public final Iterable<DecisionCombiningAlgorithm<? extends CompositeDecisionRule>> getPolicyAlgorithms(){
		return policyAlgo.values();
	}

	@Override
	public final Iterable<DecisionCombiningAlgorithm<Rule>> getRuleAlgorithms(){
		return ruleAlgo.values();
	}
	
}
