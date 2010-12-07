package com.artagon.xacml.v3.marshall;

import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.combine.DefaultDecisionCombiningAlgorithms;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyDecisionCombiningAlgorithms;
import com.artagon.xacml.v3.policy.function.DefaultFunctionProvider;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.combine.AggregatingDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.function.AggregatingFunctionProvider;

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
			FunctionProvider extendsionFunctions,
			DecisionCombiningAlgorithmProvider extensionCombiningAlgorithms) throws Exception
	{
		this.functions = (extendsionFunctions == null)?new DefaultFunctionProvider():new AggregatingFunctionProvider(
				new DefaultFunctionProvider(), extendsionFunctions);
		this.combingingAlgorithms = (extensionCombiningAlgorithms == null)? 
				new AggregatingDecisionCombiningAlgorithmProvider(
						new DefaultDecisionCombiningAlgorithms(), 
						new LegacyDecisionCombiningAlgorithms()):
				new AggregatingDecisionCombiningAlgorithmProvider(
						new DefaultDecisionCombiningAlgorithms(), 
						new LegacyDecisionCombiningAlgorithms(), 
						extensionCombiningAlgorithms);
	}
	
	protected PolicyUnmarshallerSupport() throws Exception
	{
		this(null, null);
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
	protected final DecisionCombiningAlgorithm<CompositeDecisionRule> createPolicyCombingingAlgorithm(
			String algorithmId) throws XacmlSyntaxException {
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm = combingingAlgorithms
				.getPolicyAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new XacmlSyntaxException(
					"Policy comnbining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}
}
