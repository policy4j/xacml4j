package com.artagon.xacml.v3;

public interface DecisionCombinerParameters extends PolicyElement
{
	/**
	 * Gets parameter by name
	 *  
	 * @param name a parameter name
	 * @return {@link CombinerParameter} instance
	 * or <code>null</code> if parameter with such name
	 * is not defined
	 */
	CombinerParameter getParameter(String name);
}
