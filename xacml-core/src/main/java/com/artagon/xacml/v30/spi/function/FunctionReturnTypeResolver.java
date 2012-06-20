package com.artagon.xacml.v30.spi.function;

import java.util.List;

import com.artagon.xacml.v30.ValueType;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionSpec;


/**
 * A function return type resolver
 * 
 * @author Giedrius Trumpickas
 */
public interface FunctionReturnTypeResolver 
{
	/**
	 * Resolves a function return type dynamically based
	 * on function invocation arguments
	 * 
	 * @param spec a function specification
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} function return type
	 */
	ValueType resolve(FunctionSpec spec, List<Expression> arguments);
}
