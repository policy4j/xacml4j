package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;

public interface FunctionSpecBuilder 
{
	FunctionSpec build(ValueType returnType, FunctionInvocation invocation);
	FunctionSpec build(FunctionReturnTypeResolver returnType, FunctionInvocation invocation);
}