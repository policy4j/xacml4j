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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Rule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import com.google.common.base.Preconditions;


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
	DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyAlgorithm(String algorithmId);

	/**
	 * Gets identifiers of all supported XACML
	 * rule combining algorithms by this provider
	 *
	 * @return a {@link Set} with identifiers
	 * of all supported XACML rule combining algorithms
	 */
	Set<String> getSupportedRuleAlgorithms();

	/**
	 * Gets identifiers of all supported XACML
	 * policy combining algorithms by this provider
	 *
	 * @return a {@link Set} with identifiers
	 * of all supported XACML policy combining algorithms
	 */
	Set<String> getSupportedPolicyAlgorithms();

	/**
	 * Tests if a given XACML rule combining
	 * algorithm is supported by this provider
	 *
	 * @param algorithmId an algorithm identifier
	 * @return {@code true} if algorithm
	 * is supported and {@code false} otherwise
	 */
	boolean isRuleAlgorithmProvided(String algorithmId);

	/**
	 * Tests if a given XACML policy combining
	 * algorithm is supported by this provider
	 *
	 * @param algorithmId an algorithm identifier
	 * @return {@code true} if algorithm
	 * is supported and {@code false} otherwise
	 */
	boolean isPolicyAlgorithmProvided(String algorithmId);

	static Builder builder() {
		return new Builder();
	}

	final class Builder
	{
		private Collection<DecisionCombiningAlgorithmProvider> providers;
		private AnnotatedDecisionCombiningAlgorithmProviderFactory factory;

		private Builder(){
			this.providers = new LinkedList<>();
			this.factory = new AnnotatedDecisionCombiningAlgorithmProviderFactory();
		}

		/**
		 * Creates an empty builder with no algorithms
		 *
		 * @return {@link Builder}
		 */
		public static Builder builder(){
			return new Builder();
		}

		/**
		 * Creates a builder with default XACML 3.0 & 2.0 decision
		 * combining algorithms
		 *
		 * @return {@link Builder} with
		 * a XACML 3.0 & 2.0 decision combining algorithms
		 */
		public Builder withDefaultAlgorithms(){
			this.providers.add(new DefaultXacml30DecisionCombiningAlgorithms());
			return this;
		}

		public Builder withAlgorithmProvider(
				Object provider){
			Preconditions.checkNotNull(provider instanceof DecisionCombiningAlgorithmProvider
					|| provider instanceof Class<?>);
			if(provider instanceof Class<?>){
				this.providers.add(factory.create((Class<?>)provider));
				return this;
			}
			this.providers.add((DecisionCombiningAlgorithmProvider)provider);
			return this;
		}

		public Builder withAlgorithmProviders(
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
}
