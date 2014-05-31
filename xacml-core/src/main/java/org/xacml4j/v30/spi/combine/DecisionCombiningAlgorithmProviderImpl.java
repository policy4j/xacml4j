package org.xacml4j.v30.spi.combine;

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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Rule;


/**
 * A base implementation of {@link DecisionCombiningAlgorithmProvider}
 *
 * @author Giedrius Trumpickas
 */
public class DecisionCombiningAlgorithmProviderImpl implements DecisionCombiningAlgorithmProvider
{
	private Map<String, DecisionCombiningAlgorithm<Rule>> ruleAlgo;
	private Map<String, DecisionCombiningAlgorithm<CompositeDecisionRule>> policyAlgo;

	protected DecisionCombiningAlgorithmProviderImpl(){
		this.ruleAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<Rule>>();
		this.policyAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<CompositeDecisionRule>>();
	}

	public DecisionCombiningAlgorithmProviderImpl(
			Collection<DecisionCombiningAlgorithm<Rule>> ruleAlgorithms,
			Collection<DecisionCombiningAlgorithm<CompositeDecisionRule>> policyAlgorithms){
		this.ruleAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<Rule>>();
		for(DecisionCombiningAlgorithm<Rule> algo : ruleAlgorithms){
			addRuleCombineAlgorithm(algo);
		}
		this.policyAlgo = new ConcurrentHashMap<String, DecisionCombiningAlgorithm<CompositeDecisionRule>>();
		for(DecisionCombiningAlgorithm<CompositeDecisionRule> algo : policyAlgorithms){
			addCompositeRuleCombineAlgorithm(algo);
		}
	}

	@Override
	public final DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyAlgorithm(
			String algorithmId) {
		return policyAlgo.get(algorithmId);
	}

	@Override
	public final DecisionCombiningAlgorithm<Rule> getRuleAlgorithm(String algorithmId) {
		return ruleAlgo.get(algorithmId);
	}


	@Override
	public final boolean isRuleAlgorithmProvided(String algorithmId){
		return ruleAlgo.containsKey(algorithmId);
	}

	@Override
	public final boolean isPolicyAlgorithmProvided(String algorithmId){
		return policyAlgo.containsKey(algorithmId);
	}

	public final void addRuleCombineAlgorithm(
			DecisionCombiningAlgorithm<Rule> algorithm)
	{
		DecisionCombiningAlgorithm<Rule> oldAlgo = ruleAlgo.put(algorithm.getId(), algorithm);
		if(oldAlgo != null){
			throw new IllegalArgumentException(
					String.format("Rule algorithm with identifier=\"%s\" already exist", algorithm));
		}
	}

	public final void addCompositeRuleCombineAlgorithm(
			DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm)
	{
		DecisionCombiningAlgorithm<CompositeDecisionRule> oldAlgo = policyAlgo.put(
				algorithm.getId(), algorithm);
		if(oldAlgo != null){
			throw new IllegalArgumentException(
					String.format("Policy decision combining" +
							" algorithm with identifier=\"%s\" already exist", algorithm));
		}
	}

	@Override
	public final Set<String> getSupportedPolicyAlgorithms(){
		return Collections.unmodifiableSet(policyAlgo.keySet());
	}

	@Override
	public final Set<String> getSupportedRuleAlgorithms(){
		return Collections.unmodifiableSet(ruleAlgo.keySet());
	}

}
