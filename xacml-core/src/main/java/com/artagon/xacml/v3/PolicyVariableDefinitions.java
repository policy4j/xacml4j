package com.artagon.xacml.v3;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

public final class PolicyVariableDefinitions 
{
	private Map<String, VariableDefinition> variables;
	
	public PolicyVariableDefinitions(){
		this.variables = new HashMap<String, VariableDefinition>();
	}
	
	public VariableDefinition getVariable(String variableId){
		return this.variables.get(variableId);
	}
	
	public void addVariable(VariableDefinition variable)
	{
		Preconditions.checkNotNull(variable);
		this.variables.put(variable.getVariableId(), variable);
	}
}
