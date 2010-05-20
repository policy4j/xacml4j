package com.artagon.xacml.v3;



public interface FunctionReturnTypeResolver 
{
	ValueType resolve(FunctionSpec spec, Expression ...arguments);
}
