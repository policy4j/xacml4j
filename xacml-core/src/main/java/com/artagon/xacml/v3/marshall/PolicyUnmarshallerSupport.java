package com.artagon.xacml.v3.marshall;

import com.artagon.xacml.v3.ReferencableDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.function.FunctionProvider;
import com.google.common.base.Preconditions;

/**
 * A support class for dealing with XACML 
 * functions and decision combining algorithms
 * 
 * @author Giedrius Trumpickas
 */
public class PolicyUnmarshallerSupport 
{
	private FunctionProvider functions;
	private DecisionCombiningAlgorithmProvider combingingAlgorithms;

	protected PolicyUnmarshallerSupport(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionCombiningAlgorithms) throws Exception
	{
		Preconditions.checkNotNull(functions, 
				"Function provider can't be null");
		Preconditions.checkNotNull(decisionCombiningAlgorithms, 
				"Decision combingin algorithm provider can't be null");
		this.functions = functions;
		this.combingingAlgorithms = decisionCombiningAlgorithms;
	}
	
	/**
	 * Creates function from a given identifier
	 * 
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} instance
	 * @throws XacmlSyntaxException if function with a given
	 * identifier is not known to this factory
	 */
	protected final FunctionSpec createFunction(String functionId)
			throws XacmlSyntaxException 
	{
		FunctionSpec spec = functions.getFunction(functionId);
		if (spec == null) {
			throw new XacmlSyntaxException(
					"Function with id=\"%s\" can not be resolved", functionId);
		}
		return spec;
	}
	
	/**
	 * Creates {@link DecisionCombiningAlgorithmProvider} based
	 * on a given algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithmProvider} instance
	 * @throws XacmlSyntaxException if no algorithm can be found
	 * for given identifier
	 */
	protected final DecisionCombiningAlgorithm<Rule> createRuleCombingingAlgorithm(
			String algorithmId) throws XacmlSyntaxException {
		DecisionCombiningAlgorithm<Rule> algorithm = combingingAlgorithms
				.getRuleAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new XacmlSyntaxException(
					"Rule comnbining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}
	
	/**
	 * Creates {@link DecisionCombiningAlgorithmProvider} based
	 * on a given algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithmProvider} instance
	 * @throws XacmlSyntaxException if no algorithm can be found
	 * for given identifier
	 */
	protected final DecisionCombiningAlgorithm<ReferencableDecisionRule> createPolicyCombingingAlgorithm(
			String algorithmId) throws XacmlSyntaxException {
		DecisionCombiningAlgorithm<ReferencableDecisionRule> algorithm = combingingAlgorithms
				.getPolicyAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new XacmlSyntaxException(
					"Policy comnbining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}
}
