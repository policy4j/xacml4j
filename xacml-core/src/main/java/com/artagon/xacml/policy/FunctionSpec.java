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
	 * Gets function parameter specifications.
	 * 
	 * @return list of {@link ParamSpec} instances
	 */
	List<ParamSpec> getParamSpecs();
		
	/**
	 * Resolves function return type based
	 * on a given function arguments
	 * 
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} a function return type
	 */
	ValueType getReturnType(List<Expression> arguments);
	
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
