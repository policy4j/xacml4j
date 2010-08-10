package com.artagon.xacml.v3.marshall;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.policy.combine.DefaultDecisionCombiningAlgorithms;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyDecisionCombiningAlgorithms;
import com.artagon.xacml.v3.policy.function.DefaultFunctionProvider;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.combine.AggregatingDecisionCombiningAlgorithmProvider;
import com.google.common.base.Preconditions;

public class PolicyMapperSupport 
{
	private FunctionProvider functions;
	private DecisionCombiningAlgorithmProvider combingingAlgorithms;

	protected PolicyMapperSupport(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider combiningAlgorithms) 
	{
		Preconditions.checkNotNull(functions);
		Preconditions.checkNotNull(combiningAlgorithms);
		this.functions = functions;
		this.combingingAlgorithms = combiningAlgorithms;
	}
	
	protected PolicyMapperSupport()
	{
		this(new DefaultFunctionProvider(), 
				new AggregatingDecisionCombiningAlgorithmProvider(
						new DefaultDecisionCombiningAlgorithms(), 
						new LegacyDecisionCombiningAlgorithms()));
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
