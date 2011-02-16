package com.artagon.xacml.v30.spi.combine;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.base.Preconditions;

public class DecisionCombiningAlgorithmProviderBuilder 
{
	private Collection<DecisionCombiningAlgorithmProvider> providers;
	
	private DecisionCombiningAlgorithmProviderBuilder(){
		this.providers = new LinkedList<DecisionCombiningAlgorithmProvider>();
	}
	
	public static DecisionCombiningAlgorithmProviderBuilder builder(){
		return new DecisionCombiningAlgorithmProviderBuilder();
	}
	
	public DecisionCombiningAlgorithmProviderBuilder withDefaultAlgorithms(){
		this.providers.add(new DefaultXacml30DecisionCombiningAlgorithms());
		return this;
	}
	
	public DecisionCombiningAlgorithmProviderBuilder withAlgorithmProvider(
			DecisionCombiningAlgorithmProvider p){
		Preconditions.checkNotNull(p);
		this.providers.add(p);
		return this;
	}
	
	public DecisionCombiningAlgorithmProvider build(){
		return new AggregatingDecisionCombiningAlgorithmProvider(providers);
	}
}
