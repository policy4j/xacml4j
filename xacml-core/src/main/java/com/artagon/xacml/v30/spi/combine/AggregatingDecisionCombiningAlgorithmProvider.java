package com.artagon.xacml.v30.spi.combine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.Rule;
import com.google.common.base.Preconditions;

/**
 * An implementation of {@link DecisionCombiningAlgorithmProvider}
 * which aggregates other instances of {@link DecisionCombiningAlgorithmProvider}
 * 
 * @author Giedrius Trumpickas
 */
final class AggregatingDecisionCombiningAlgorithmProvider 
	implements DecisionCombiningAlgorithmProvider
{
	private Map<String, DecisionCombiningAlgorithmProvider> ruleAlgorithms;
	private Map<String, DecisionCombiningAlgorithmProvider> policyAlgorithms;
	
	public AggregatingDecisionCombiningAlgorithmProvider(){
		this.ruleAlgorithms = new ConcurrentHashMap<String, DecisionCombiningAlgorithmProvider>();
		this.policyAlgorithms = new ConcurrentHashMap<String, DecisionCombiningAlgorithmProvider>();
	}
	
	/**
	 * Constructs an aggregating decision algorithm provider
	 * 
	 * @param providers an array of providers
	 */
	public AggregatingDecisionCombiningAlgorithmProvider(
			DecisionCombiningAlgorithmProvider ...providers){
		this(Arrays.asList(providers));
	}
	
	/**
	 * Constructs an aggregating decision algorithm provider
	 * 
	 * @param providers an array of providers
	 */
	public AggregatingDecisionCombiningAlgorithmProvider(
			Collection<DecisionCombiningAlgorithmProvider> providers){
		this();
		Preconditions.checkNotNull(providers);
		for(DecisionCombiningAlgorithmProvider p : providers){
			add(p);
		}
	}
	
	/**
	 * Adds new {@link DecisionCombiningAlgorithmProvider} to this
	 * composite provider, imports all algorithms from a given provider
	 * 
	 * @param p a decision combine algorithm provider
	 */
	public final void add(DecisionCombiningAlgorithmProvider p)
	{
		for(String algorithmId : p.getSupportedPolicyAlgorithms()){
			if(policyAlgorithms.containsKey(algorithmId)){
				throw new IllegalArgumentException(String.format(
						"Provider already contains policy decision " +
						"combining algorithm=\"%s\"", algorithmId));
			}
			this.policyAlgorithms.put(algorithmId, p);
		}
		for(String algorithmId : p.getSupportedRuleAlgorithms()){
			if(ruleAlgorithms.containsKey(algorithmId)){
				throw new IllegalArgumentException(String.format(
						"Provider already contains rule decision " +
						"combining algorithm=\"%s\"", algorithmId));
			}
			this.ruleAlgorithms.put(algorithmId, p);
		}
	}
	
	@Override
	public final DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyAlgorithm(
			String algorithmId) 
	{
		DecisionCombiningAlgorithmProvider p = policyAlgorithms.get(algorithmId);
		return (p == null)?null:p.getPolicyAlgorithm(algorithmId);
	}

	@Override
	public final DecisionCombiningAlgorithm<Rule> getRuleAlgorithm(String algorithmId) 
	{
		DecisionCombiningAlgorithmProvider p = ruleAlgorithms.get(algorithmId);
		return (p == null)?null:p.getRuleAlgorithm(algorithmId);
	}

	@Override
	public final Set<String> getSupportedPolicyAlgorithms() {
		return Collections.unmodifiableSet(policyAlgorithms.keySet());
	}

	@Override
	public final Set<String> getSupportedRuleAlgorithms() {
		return Collections.unmodifiableSet(ruleAlgorithms.keySet());
	}

	@Override
	public final boolean isPolicyAgorithmProvided(String algorithmId) {
		return policyAlgorithms.containsKey(algorithmId);
	}

	@Override
	public final boolean isRuleAgorithmProvided(String algorithmId) {
		return ruleAlgorithms.containsKey(algorithmId);
	}
	
}
