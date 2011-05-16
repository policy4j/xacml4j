package com.artagon.xacml.v30.spi.combine;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.base.Preconditions;

public final class DecisionCombiningAlgorithmProviderBuilder 
{
	private Collection<DecisionCombiningAlgorithmProvider> providers;
	private AnnotatedDecisionCombiningAlgorithmProviderFactory factory;
	
	private DecisionCombiningAlgorithmProviderBuilder(){
		this.providers = new LinkedList<DecisionCombiningAlgorithmProvider>();
		this.factory = new AnnotatedDecisionCombiningAlgorithmProviderFactory();
	}
	
	/**
	 * Creates an empty builder with no algorithms
	 * 
	 * @return {@link DecisionCombiningAlgorithmProviderBuilder}
	 */
	public static DecisionCombiningAlgorithmProviderBuilder builder(){
		return new DecisionCombiningAlgorithmProviderBuilder();
	}
	
	/**
	 * Creates a builder with default XACML 3.0 & 2.0 decision
	 * combining algorithms
	 * 
	 * @return {@link DecisionCombiningAlgorithmProviderBuilder} with
	 * a XACML 3.0 & 2.0 decision combining algorithms
	 */
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
	
	/**
	 * Adds an annotated decision combining algorithms to this builder
	 * 
	 * @param clazz a class containing an implementation of decision
	 * combining algorithms
	 * 
	 * @return {@link DecisionCombiningAlgorithmProviderBuilder}
	 */
	public DecisionCombiningAlgorithmProviderBuilder withAlgorithmProvider(
			Class<?> clazz){
		Preconditions.checkNotNull(clazz);
		this.providers.add(factory.create(clazz));
		return this;
	}
	
	public DecisionCombiningAlgorithmProviderBuilder withAlgorithmProviders(
			Iterable<DecisionCombiningAlgorithmProvider> providers){
		for(DecisionCombiningAlgorithmProvider p : providers){
			withAlgorithmProvider(p);
		}
		return this;
	}
	
	public DecisionCombiningAlgorithmProvider build(){
		return new AggregatingDecisionCombiningAlgorithmProvider(providers);
	}
}
