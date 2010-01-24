package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.FunctionSpec;

public interface FunctionSpecBuilder 
{
	FunctionSpec build(RegularFunction implementation);
	FunctionSpec build(DynamicFunction implementation);
}