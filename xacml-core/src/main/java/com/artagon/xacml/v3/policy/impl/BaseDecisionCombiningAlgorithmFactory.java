package com.artagon.xacml.v3.policy.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithmResolver;
import com.artagon.xacml.v3.policy.Rule;

public class BaseDecisionCombiningAlgorithmFactory implements DecisionCombiningAlgorithmResolver
{
	private Map<String, DecisionCombiningAlgorithm<Rule>> ruleAlgorithms;
	private Map<String, DecisionCombiningAlgorithm<? extends CompositeDecisionRule>> compositeDecisionAlgorithms;
	
	protected BaseDecisionCombiningAlgorithmFactory(){
		this.ruleAlgorithms = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<Rule>>();
		this.compositeDecisionAlgorithms = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<? extends CompositeDecisionRule>>();
	}
	
	@Override
	public final DecisionCombiningAlgorithm<? extends CompositeDecisionRule> getPolicyAlgorithm(
			String algorithmId) {
		Preconditions.checkNotNull(algorithmId);
		return compositeDecisionAlgorithms.get(algorithmId);
	}

	@Override
	public final DecisionCombiningAlgorithm<Rule> getRuleAlgorithm(String algorithmId) {
		Preconditions.checkNotNull(algorithmId);
		return ruleAlgorithms.get(algorithmId);
	}

	@Override
	public final Set<String> getSupprtedPolicyAlgorithms() {
		return Collections.unmodifiableSet(compositeDecisionAlgorithms.keySet());
	}

	@Override
	public final Set<String> getSupprtedRuleAlgorithms() {
		return Collections.unmodifiableSet(ruleAlgorithms.keySet());
	}
	
	
}
