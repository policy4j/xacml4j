package com.artagon.xacml.v30;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;

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
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("parameters", parameters.values())
		.toString();
	}
	
	@Override
	public int hashCode(){
		return parameters.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof CombinerParameters)){
			return false;
		}
		CombinerParameters c = (CombinerParameters)o;
		return parameters.equals(c.parameters);
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
