package org.xacml4j.v30.xml;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.VariableDefinition;

import com.google.common.base.Preconditions;

class VariableManager<VExpression>
{
	private final Map<String, VExpression> variableDefinitionExpressions;
	private final Map<String, VariableDefinition> variableDefinitions;
	private final Stack<String> resolutionStack;

	public VariableManager(Map<String, VExpression> variableDefinitionExpressions) {
		this.variableDefinitionExpressions = new HashMap<>(variableDefinitionExpressions);
		this.variableDefinitions = new HashMap<>(variableDefinitionExpressions.size());
		this.resolutionStack = new Stack<>();
	}

	public VariableDefinition getVariableDefinition(String variableId) {
		return variableDefinitions.get(variableId);
	}

	public VExpression getVariableDefinitionExpression(String variableId) {
		return this.variableDefinitionExpressions.get(variableId);
	}

	public Iterable<String> getVariableDefinitionExpressions() {
		return variableDefinitionExpressions.keySet();
	}

	public void pushVariableDefinition(String variableId) {
		if (resolutionStack.contains(variableId)) {
			throw new XacmlSyntaxException("Cyclic variable reference=\"%s\" detected", variableId);
		}
		this.resolutionStack.push(variableId);
	}

	public Map<String, VariableDefinition> getVariableDefinitions() {
		return Collections.unmodifiableMap(variableDefinitions);
	}

	public String resolveVariableDefinition(VariableDefinition variableDef) {
		Preconditions.checkArgument(resolutionStack.peek().equals(variableDef.getVariableId()));
		this.variableDefinitions.put(variableDef.getVariableId(), variableDef);
		return resolutionStack.pop();
	}
}
