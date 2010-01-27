package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;

public interface FunctionReturnTypeResolutionCallback 
{
	ValueType resolve(FunctionSpec spec, Expression ...arguments);
}
