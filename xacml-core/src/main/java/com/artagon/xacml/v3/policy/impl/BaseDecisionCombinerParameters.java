package com.artagon.xacml.v3.policy.impl;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v3.CombinerParameter;
import com.artagon.xacml.v3.DecisionCombinerParameters;
import com.artagon.xacml.v3.XacmlObject;

abstract class BaseDecisionCombinerParameters extends XacmlObject 
	implements DecisionCombinerParameters
{
	protected Map<String, CombinerParameter> parameters;
	
	protected BaseDecisionCombinerParameters(
			Iterable<CombinerParameter> parameters){
		this.parameters = new HashMap<String, CombinerParameter>();
		for(CombinerParameter p : parameters){
			this.parameters.put(p.getName(), p);
		}
	}

	
	@Override
	public final CombinerParameter getParameter(String name) {
		return parameters.get(name);
	}
}
