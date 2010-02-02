package com.artagon.xacml.v3.policy;


public interface FunctionInvocation
{
	<T extends Value> T invoke(FunctionSpec spoec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
