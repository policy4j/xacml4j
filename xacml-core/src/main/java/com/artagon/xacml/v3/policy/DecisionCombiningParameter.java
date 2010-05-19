package com.artagon.xacml.v3.policy;

import com.google.common.base.Preconditions;

public class DecisionCombiningParameter 
{
	private String name;
	private AttributeValue value;
	
	public DecisionCombiningParameter(String name, AttributeValue value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Gets parameter name
	 * 
	 * @return parameter name
	 */
	public final String getName(){
		return name;
	}
	
	/**
	 * Gets parameter value
	 * 
	 * @return parameter value
	 */
	public final AttributeValue getValue(){
		return value;
	}
}
