package org.xacml4j.v30.spi.combine;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import java.util.Set;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.Rule;


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
	boolean isRuleAgorithmProvided(String algorithmId);

	/**
	 * Tests if a given XACML policy combining
	 * algorithm is supported by this provider
	 *
	 * @param algorithmId an algorithm identifier
	 * @return {@code true} if algorithm
	 * is supported and {@code false} otherwise
	 */
	boolean isPolicyAgorithmProvided(String algorithmId);
}
