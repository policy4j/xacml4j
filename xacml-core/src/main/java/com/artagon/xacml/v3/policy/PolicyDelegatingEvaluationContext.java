package com.artagon.xacml.v3.policy;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.util.Preconditions;

class PolicyDelegatingEvaluationContext extends DelegatingEvaluationContext
{	
	private Map<String, Value> variableCache;
	private Policy policy;
	
	/**
	 * Creates policy evaluation context with a given parent context
	 * 
	 * @param context a parent evaluation context
	 * @param policy a policy to be evaluated
	 */
	PolicyDelegatingEvaluationContext(EvaluationContext context, Policy policy)
	{
		super(context);
		Preconditions.checkNotNull(policy);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		this.policy = policy;
		this.variableCache = new HashMap<String, Value>();
	}

	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}

	@Override
	public VariableDefinition getVariableDefinition(String variableId) {
		Preconditions.checkNotNull(variableId);
		return policy.getVariableDefinition(variableId);
	}

	@Override
	public Value getVariableEvaluationResult(String variableId) {
		Preconditions.checkNotNull(variableId);
		return variableCache.get(variableId);
	}

	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
		Preconditions.checkNotNull(variableId);
		Preconditions.checkNotNull(value);
		this.variableCache.put(variableId, value);
	}
}
