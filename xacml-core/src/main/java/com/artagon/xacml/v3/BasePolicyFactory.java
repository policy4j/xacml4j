package com.artagon.xacml.v3;

import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.google.common.base.Preconditions;

public abstract class BasePolicyFactory implements PolicyFactory 
{
	private FunctionProvider functions;
	private DecisionCombiningAlgorithmProvider combingingAlgorithms;

	protected BasePolicyFactory(
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider combiningAlgorithms) {
		Preconditions.checkNotNull(functions);
		Preconditions.checkNotNull(combiningAlgorithms);
		this.functions = functions;
		this.combingingAlgorithms = combiningAlgorithms;
	}

	/**
	 * Creates function from a given identifier
	 * 
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} instance
	 * @throws PolicySyntaxException if function with a given
	 * identifier is not known to this factory
	 */
	protected final FunctionSpec createFunction(String functionId)
			throws PolicySyntaxException 
	{
		FunctionSpec spec = functions.getFunction(functionId);
		if (spec == null) {
			throw new PolicySyntaxException(
					"Function with id=\"%s\" can not be resolved", functionId);
		}
		return spec;
	}
	
	protected final DecisionCombiningAlgorithm<Rule> createRuleCombingingAlgorithm(
			String algorithmId) throws PolicySyntaxException {
		DecisionCombiningAlgorithm<Rule> algorithm = combingingAlgorithms
				.getRuleAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new PolicySyntaxException(
					"Rule comnbining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}

	protected final DecisionCombiningAlgorithm<CompositeDecisionRule> createPolicyCombingingAlgorithm(
			String algorithmId) throws PolicySyntaxException {
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm = combingingAlgorithms
				.getPolicyAlgorithm(algorithmId);
		if (algorithm == null) {
			throw new PolicySyntaxException(
					"Policy comnbining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}

}
