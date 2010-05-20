package com.artagon.xacml.v3.policy.impl;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.DecisionCombinerParameter;
import com.artagon.xacml.v3.policy.DecisionCombinerParameters;

abstract class BaseDecisionCombinerParameters extends XacmlObject 
	implements DecisionCombinerParameters
{
	protected Map<String, DecisionCombinerParameter> parameters;
	
	protected BaseDecisionCombinerParameters(
			Iterable<DecisionCombinerParameter> parameters){
		this.parameters = new HashMap<String, DecisionCombinerParameter>();
		for(DecisionCombinerParameter p : parameters){
			this.parameters.put(p.getName(), p);
		}
	}

	@Override
	public final DecisionCombinerParameter getParameter(String name) {
		return parameters.get(name);
	}
}
