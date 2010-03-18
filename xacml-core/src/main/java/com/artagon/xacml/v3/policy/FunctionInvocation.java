package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Expression;


public interface FunctionInvocation
{
	<T extends Value> T invoke(FunctionSpec spoec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
