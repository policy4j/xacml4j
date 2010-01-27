package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Value;

public interface FunctionInvocation
{
	<T extends Value> T invoke(FunctionSpec spoec, 
			EvaluationContext context, Expression ...arguments) 
		throws EvaluationException;
}
