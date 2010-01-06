package com.artagon.xacml.policy;


public interface FunctionSpecBuilder 
{
	<R extends Value> FunctionSpec build(FunctionInvocation implementation);
}