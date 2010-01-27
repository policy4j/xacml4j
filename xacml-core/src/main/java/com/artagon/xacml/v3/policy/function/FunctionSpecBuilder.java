package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;

public interface FunctionSpecBuilder 
{
	FunctionSpec build(ValueType returnType, FunctionInvocationCallback invocation);
	FunctionSpec build(FunctionReturnTypeResolutionCallback returnType, FunctionInvocationCallback invocation);
}