package com.artagon.xacml.v30.policy;

import java.util.Collection;

public interface Policy extends CompositeDecision
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