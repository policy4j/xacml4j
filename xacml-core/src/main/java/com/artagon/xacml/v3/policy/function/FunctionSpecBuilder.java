package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.FunctionSpec;

public interface FunctionSpecBuilder 
{
	FunctionSpec build(StaticallyTypedFunction implementation);
	FunctionSpec build(DynamicallyTypedFunction implementation);
}