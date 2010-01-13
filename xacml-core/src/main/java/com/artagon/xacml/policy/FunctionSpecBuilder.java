package com.artagon.xacml.policy;


public interface FunctionSpecBuilder 
{
	FunctionSpec build(RegularFunction implementation);
	FunctionSpec build(DynamicFunction implementation);
}