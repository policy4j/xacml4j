package com.artagon.xacml.v3;

import java.util.HashMap;
import java.util.Map;

abstract class BaseDecisionCombinerParameters extends XacmlObject 
	implements PolicyElement
{
	protected Map<String, CombinerParameter> parameters;
	
	protected BaseDecisionCombinerParameters(
			Iterable<CombinerParameter> parameters){
		this.parameters = new HashMap<String, CombinerParameter>();
		for(CombinerParameter p : parameters){
			this.parameters.put(p.getName(), p);
		}
	}

	
	/**
	 * Gets parameter by name
	 *  
	 * @param name a parameter name
	 * @return {@link CombinerParameter} instance
	 * or <code>null</code> if parameter with such name
	 * is not defined
	 */
	public CombinerParameter getParameter(String name) {
		return parameters.get(name);
	}
}
