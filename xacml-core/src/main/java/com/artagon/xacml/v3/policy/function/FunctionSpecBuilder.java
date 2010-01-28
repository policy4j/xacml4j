package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;

public interface FunctionSpecBuilder 
{
	/**
	 * Builds {@link FunctionSpec} with a given
	 * {@link ValueType} as a function return type
	 * and given {@link FunctionInvocation} as a
	 * function implementation
	 * 
	 * @param returnType a function return type
	 * @param invocation a function
	 * @return {@link FunctionSpec} a f
	 */
	FunctionSpec build(ValueType returnType, 
			FunctionInvocation invocation);
	
	FunctionSpec build(FunctionReturnTypeResolver returnType, 
			FunctionInvocation invocation);
}