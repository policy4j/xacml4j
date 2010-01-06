package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

public class DecisionCombinerParameter 
{
	private String name;
	private Attribute value;
	
	public DecisionCombinerParameter(String name, Attribute value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}
	
	public final String getName(){
		return name;
	}
	
	public final Attribute getValue(){
		return value;
	}
}
