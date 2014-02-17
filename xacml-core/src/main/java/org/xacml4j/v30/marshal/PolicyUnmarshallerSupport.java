package org.xacml4j.v30.marshal;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.Types;

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
	private Types types = Types.builder().defaultTypes().create();

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
					"Policy combining algorithm=\"%s\" can not be resolved",
					algorithmId);
		}
		return algorithm;
	}

	protected Types getTypes(){
		return types;
	}
}
