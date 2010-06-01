package com.artagon.xacml.v3.policy.jaxb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.artagon.xacml.v3.VariableDefinition;
import com.google.common.base.Preconditions;

public class VariableManager <VExpression>
{
	private Map<String, VExpression> variableDefinitionExpressions;
	private Map<String, VariableDefinition> variableDefinitions;
	private Stack<String> resolutionStack;
	
	public VariableManager(Map<String, VExpression> variableDefinitionExpressions)
	{
		this.variableDefinitionExpressions = new HashMap<String, VExpression>(variableDefinitionExpressions);
		this.variableDefinitions = new HashMap<String, VariableDefinition>();
		this.resolutionStack = new Stack<String>();
	}
	
	public VariableDefinition getVariableDefinition(String variableId){
		return variableDefinitions.get(variableId);
	}
	
	public VExpression getVariableDefinitionExpression(String variableId){
		return this.variableDefinitionExpressions.get(variableId);
	}
	
	public Iterable<String> getVariableDefinitionExpressions(){
		return variableDefinitionExpressions.keySet();
	}
	
	public void pushVariableDefinition(String variableId)
	{
		if(resolutionStack.contains(variableId)){
			throw new IllegalArgumentException(String.format("Cyclic " +
					"variable reference=\"%s\" detected", variableId));
		}
		this.resolutionStack.push(variableId);
	}
	
	public Map<String, VariableDefinition> getVariableDefinitions(){
		return Collections.unmodifiableMap(variableDefinitions);
	}
	
	public String resolveVariableDefinition(VariableDefinition variableDef)
	{
		Preconditions.checkArgument(resolutionStack.peek().equals(variableDef.getVariableId()));
		this.variableDefinitions.put(variableDef.getVariableId(), variableDef);
		return resolutionStack.pop();
	}
}
