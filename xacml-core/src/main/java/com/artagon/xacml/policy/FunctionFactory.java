package com.artagon.xacml.policy;

import com.artagon.xacml.FunctionId;

public interface FunctionFactory 
{
	/**
	 * Gets function spec instance for a given function
	 * identifier.
	 * 
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} instance for a given
	 * identifier or <code>null</code> if function
	 * can not be found for a given identifier
	 */
	FunctionSpec getFunction(FunctionId functionId);
	
	/**
	 * Tests if given function is supported by
	 * this factory
	 * 
	 * @param functionId a function identifier
	 * @return <code>true</code> if function
	 * is supported by this factory
	 */
	boolean isSupported(FunctionId functionId);
	
	/**
	 * Gets all supported function by this factory
	 * 
	 * @return {@link Iterable} over all supported
	 * function by this factory
	 */
	Iterable<FunctionId> getSupportedFunctions();
}
