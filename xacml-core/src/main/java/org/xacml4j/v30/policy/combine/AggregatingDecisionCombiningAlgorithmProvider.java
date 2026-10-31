package org.xacml4j.v30.policy.combine;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Rule;

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
	public final boolean isPolicyAlgorithmProvided(String algorithmId) {
		return policyAlgorithms.containsKey(algorithmId);
	}

	@Override
	public final boolean isRuleAlgorithmProvided(String algorithmId) {
		return ruleAlgorithms.containsKey(algorithmId);
	}

}
