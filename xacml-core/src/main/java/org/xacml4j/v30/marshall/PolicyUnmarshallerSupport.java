package org.xacml4j.v30.marshall;

import java.util.Map;

import javax.xml.namespace.QName;

import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.DataTypes;

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
	
	/**
	 * Gets data type via date type identifier
	 * 
	 * @param typeId a data type identifier
	 * @return {@link AttributeExpType}
	 * @throws XacmlSyntaxException
	 */
	protected AttributeExpType getDataType(String typeId) 
		throws XacmlSyntaxException
	{
		return DataTypes.getType(typeId);
	}
	
	protected  AttributeExp createAttributeValue(
			String typeId, 
			Object value, Map<QName, String> values) throws XacmlSyntaxException 
	{
		AttributeExpType type = getDataType(typeId);
		try {
			return type.create(value, getXPathCategory(values));
		} catch (Exception e) {
			throw new XacmlSyntaxException(e);
		}
	}

	private  AttributeCategory getXPathCategory(Map<QName, String> attr) 
		throws XacmlSyntaxException
	{
		for (QName n : attr.keySet()) {
			if (n.getLocalPart().equals("XPathCategory")) {
				return AttributeCategories.parse(attr.get(n));
			}
		}
		return null;
	}
}
