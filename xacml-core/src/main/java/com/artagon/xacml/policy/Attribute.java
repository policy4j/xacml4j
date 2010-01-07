package com.artagon.xacml.policy;

public interface Attribute extends Value
{
	String toXacmlString();
	
	Attribute evaluate(EvaluationContext context) throws PolicyEvaluationException;
}