package com.artagon.xacml.v3.policy;

public interface Apply extends PolicyElement, Expression
{
	/**
	 * Gets XACML function identifier
	 * 
	 * @return XACML function identifier
	 */
	String getFunctionId();
}