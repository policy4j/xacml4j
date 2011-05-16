package com.artagon.xacml.v30.marshall;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.Rule;
import com.artagon.xacml.v30.VariableDefinition;
import com.artagon.xacml.v30.XacmlSyntaxException;

public interface XacmlPolicyParsingContext 
{
	/**
	 * Gets current policy identifier
	 * 
	 * @return current policy identifier
	 */
	String getPolicyId();
	
	/**
	 * Resolves an {@link AttributeValueType}
	 * based on the given type identifier
	 * 
	 * @param typeId a type identifier
	 * @return {@link AttributeValueType} instance
	 * @throws XacmlSyntaxException if type
	 * can not be resolved to the valid XACML
	 * type
	 */
	AttributeValueType getType(String typeId) 
		throws XacmlSyntaxException;
	
	<T> T create(XMLStreamReader r) 
		throws XMLStreamException;
	
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
	
	/**
	 * Gets a rule {@link DecisionCombiningAlgorithm} for a given
	 * algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithm}
	 * @throws XacmlSyntaxException if a given algorithm identifier
	 * can't be resolved to a valid {@link DecisionCombiningAlgorithm}
	 * instance
	 */
	DecisionCombiningAlgorithm<Rule> getRuleCombiningAlgorithm(
			String algorithmId) throws XacmlSyntaxException;
	
	/**
	 * Gets a policy {@link DecisionCombiningAlgorithm} for a given
	 * algorithm identifier
	 * 
	 * @param algorithmId an algorithm identifier
	 * @return {@link DecisionCombiningAlgorithm}
	 * @throws XacmlSyntaxException if a given algorithm identifier
	 * can't be resolved to a valid {@link DecisionCombiningAlgorithm}
	 * instance
	 */
	DecisionCombiningAlgorithm<CompositeDecisionRule> getPolicyCombiningAlgorithm(
			String algorithmId) throws XacmlSyntaxException;
}
