package com.artagon.xacml.v3.pdp;

public interface EnvironmentCallback 
{
	/**
	 * Gets names value from PDP environment
	 * 
	 * @param name a parameter name
	 * @return enviroment value
	 */
	Object getValue(String name);
}
