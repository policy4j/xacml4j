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

	public DecisionCombiningAlgorithmProviderBuilder withAlgorithmProviders(
			Iterable<DecisionCombiningAlgorithmProvider> providers){
		for(DecisionCombiningAlgorithmProvider p : providers){
			withAlgorithmProvider(p);
		}
		return this;
	}

	public DecisionCombiningAlgorithmProvider create(){
		return new AggregatingDecisionCombiningAlgorithmProvider(providers);
	}
}
