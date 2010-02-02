package com.artagon.xacml.v3.policy;


public interface FunctionReturnTypeResolver 
{
	ValueType resolve(FunctionSpec spec, Expression ...arguments);
}
