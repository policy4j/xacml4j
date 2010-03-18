package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Expression;


public interface FunctionReturnTypeResolver 
{
	ValueType resolve(FunctionSpec spec, Expression ...arguments);
}
