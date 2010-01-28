package com.artagon.xacml.v3.policy;

import java.util.Collection;

public interface Policy extends CompositeDecisionRule
{
	/**
	 * Gets policy variable definitions
	 * 
	 * @return a collection of {@link VariableDefinition} instances
	 */
	Collection<VariableDefinition> getVariableDefinitions();
	
	/**
	 * Gets {@link VariableDefinition} by identifier
	 * 
	 * @param variableId variable identifier
	 * @return {@link VariableDefinition} or <code>null</code>
	 * if variable definition can not be resolved
	 */
	VariableDefinition getVariableDefinition(String variableId);	
}