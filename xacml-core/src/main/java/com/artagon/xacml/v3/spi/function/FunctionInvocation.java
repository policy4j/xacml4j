package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.Value;




public interface FunctionInvocation
{
	<T extends Value> T invoke(FunctionSpec spoec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
