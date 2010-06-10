package com.artagon.xacml.v3;




public interface FunctionInvocation
{
	<T extends Value> T invoke(FunctionSpec spoec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
