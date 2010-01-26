package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

public class DecisionCombiningAlgorithmParameter 
{
	private String name;
	private AttributeValue value;
	
	public DecisionCombiningAlgorithmParameter(String name, AttributeValue value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}
	
	public final String getName(){
		return name;
	}
	
	public final AttributeValue getValue(){
		return value;
	}
}
