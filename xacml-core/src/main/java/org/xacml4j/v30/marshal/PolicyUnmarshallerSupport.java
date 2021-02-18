package org.xacml4j.v30.marshal;

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

import com.google.common.base.Preconditions;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.FunctionProvider;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;

/**
 * A support class for dealing with XACML
 * functions and decision combining algorithms
 *
 * @author Giedrius Trumpickas
 */
public class PolicyUnmarshallerSupport
{
	private FunctionProvider functions;
	private DecisionCombiningAlgorithmProvider combiningAlgorithms;

	protected PolicyUnmarshallerSupport(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionCombiningAlgorithms) throws Exception
	{
		Preconditions.checkNotNull(functions,
				"Function provider can't be null");
		Preconditions.checkNotNull(decisionCombiningAlgorithms,
				"Decision combining algorithm provider can't be null");
		this.functions = functions;
		this.combiningAlgorithms = decisionCombiningAlgorithms;
	}

	protected PolicyUnmarshallerSupport(
			DecisionCombiningAlgorithmProvider decisionCombiningAlgorithms) throws Exception
	{
		this(FunctionProvider.builder()
				.withStandardFunctions()
				.withDiscoveredFunctions()
				.build(), decisionCombiningAlgorithms);
	}

	/**
	 * Creates function from a given identifier
	 *
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} defaultProvider
	 * @throws SyntaxException if function with a given
	 * identifier is not known to this factory
	 */
	protected final FunctionSpec lookUpFunction(String functionId)
			throws SyntaxException
	{
		return functions.getFunction(functionId)
				.orElseThrow(
						()-> SyntaxException
								.invalidFunction(functionId));

	}

	/**
	 * Creates {@link DecisionCombiningAlgorithmProvider} based
	 * on a given algorithm identifier
	 *
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithmProvider} defaultProvider
	 * @throws SyntaxException if no algorithm can be found
	 * for given identifier
	 */
	protected final DecisionCombiningAlgorithm<Rule> createRuleCombiningAlgorithm(
			String algorithmId) throws SyntaxException {
		DecisionCombiningAlgorithm<Rule> algorithm = combiningAlgorithms
				.getRuleAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new SyntaxException(
					"Rule combining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}

	/**
	 * Creates {@link DecisionCombiningAlgorithmProvider} based
	 * on a given algorithm identifier
	 *
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithmProvider} defaultProvider
	 * @throws SyntaxException if no algorithm can be found
	 * for given identifier
	 */
	protected final DecisionCombiningAlgorithm<CompositeDecisionRule> lookUpPolicyCombiningAlgorithm(
			String algorithmId) throws SyntaxException {
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm = combiningAlgorithms
				.getPolicyAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new SyntaxException(
					"Policy combining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}
}
