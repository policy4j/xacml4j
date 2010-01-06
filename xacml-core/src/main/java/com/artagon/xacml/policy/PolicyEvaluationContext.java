package com.artagon.xacml.policy;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.util.Preconditions;

public class PolicyEvaluationContext extends BaseEvaluationContext
{	
	private Map<String, Value> variableCache;
	private Policy policy;

	/**
	 * Creates policy evaluation context without parent evaluation context
	 * @param policy
	 * @param service
	 */
	PolicyEvaluationContext(Policy policy, AttributeResolutionService service)
	{
		super(service);
		Preconditions.checkNotNull(policy);
		this.policy = policy;
		this.variableCache = new HashMap<String, Value>(variableCache);
	}


	
	@Override
	public VariableDefinition getVariableDefinition(String variableId){
		Preconditions.checkNotNull(variableId);
		return policy.getVariableDefinition(variableId);
	}
	
	@Override
	public Value getVariableEvaluationResult(String variableId) {
		return variableCache.get(variableId);
	}
	
	@Override
	public void setVariableEvaluationResult(String variableId, Value value) {
		this.variableCache.put(variableId, value);
	}
}
