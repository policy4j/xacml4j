package com.artagon.xacml.v3;

public interface VariableReference extends Expression 
{
	/**
	 * Gets variable identifier.
	 * 
	 * @return variable identifier
	 */
	String getVariableId();

}