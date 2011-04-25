package com.artagon.xacml.v30.marshall.jaxb;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.VariableDefinition;
import com.artagon.xacml.v30.XacmlSyntaxException;

public interface XacmlPolicyParsingContext extends XacmlParsingContext
{
	/**
	 * Gets current policy identifier
	 * 
	 * @return current policy identifier
	 */
	String getPolicyId();
	
	/**
	 * Gets policy variable by the identifier
	 * 
	 * @param variableId a variable identifier
	 * @return {@link VariableDefinition} instance
	 * @throws XacmlSyntaxException if variable
	 * can not be resolved
	 */
	VariableDefinition getVariable(String variableId) 
		throws XacmlSyntaxException;
	
	/**
	 * Gets function by this identifier
	 * 
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} instance
	 * @throws XacmlSyntaxException if function
	 * can not be resolved
	 */
	FunctionSpec getFunction(String functionId) 
		throws XacmlSyntaxException;
	
	DecisionCombiningAlgorithm<Rule> getRuleCombiningAlgorithm(
			String algorithmId) throws XacmlSyntaxException;
	
	DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyCombiningAlgorithm(
			String algorithmId) throws XacmlSyntaxException;
}
