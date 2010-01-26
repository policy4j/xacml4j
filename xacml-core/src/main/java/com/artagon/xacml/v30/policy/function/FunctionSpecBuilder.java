package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.policy.FunctionSpec;

public interface FunctionSpecBuilder 
{
	FunctionSpec build(StaticallyTypedFunction implementation);
	FunctionSpec build(DynamicallyTypedFunction implementation);
}