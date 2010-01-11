package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;


/**
 * A XACML function specification
 *
 * @param <ReturnType>
 */
public interface FunctionSpec
{
	/**
	 * Gets function identifier.
	 * 
	 * @return XACML function identifier
	 */
	FunctionId getId();
	
	/**
	 * Tests if a given function represents
	 * a higher order function
	 * 
	 * @return <code>true</code> if a given
	 * spec is for a higher order function
	 */
	boolean isHigherOrderFunction();
	
	/**
	 * Gets function parameter specifications.
	 * 
	 * @return list of {@link ParamSpec} instances
	 */
	List<ParamSpec> getParamSpecs();
		
	/**
	 * Gets number of function formal 
	 * parameters
	 * 
	 * @return gets number of function formal
	 * parameters
	 */
	int getNumberOfParams();
		
	/**
	 * Gets function implementation
	 * 
	 * @return {@link FunctionImplementation} function
	 * implementation
	 */
	FunctionImplementation getImplementation();
	
	/**
	 * Validates list of parameters.
	 * 
	 * @param params a list of parameters
	 * @return <code>true</code> if given list
	 * can be used as an arguments to function described
	 * by this specification.
	 */
	boolean validateParameters(List<Expression> arguments);
	
	/**
	 * Validates list of parameters.
	 * 
	 * @param params a list of parameters
	 * @return <code>true</code> if given list
	 * can be used as an arguments to function described
	 * by this specification.
	 */
	boolean validateParameters(Expression ... expressions);
}
