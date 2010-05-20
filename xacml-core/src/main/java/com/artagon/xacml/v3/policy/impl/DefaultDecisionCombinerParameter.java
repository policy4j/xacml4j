package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.CombinerParameter;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

/**
 * Conveys a single parameter for a policy- or rule-combining algorithm.
 * 
 * @author Giedrius Trumpickas
 */
final class DefaultDecisionCombinerParameter extends XacmlObject 
	implements CombinerParameter
{
	private String name;
	private AttributeValue value;
	
	/**
	 * Constructs decision combining parameter
	 * 
	 * @param name a parameter name
	 * @param value a parameter value
	 */
	public DefaultDecisionCombinerParameter(String name, AttributeValue value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}
	
	@Override
	public final String getName(){
		return name;
	}
	
	@Override
	public final AttributeValue getValue(){
		return value;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
}
