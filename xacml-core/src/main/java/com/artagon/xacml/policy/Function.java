package com.artagon.xacml.policy;

import java.util.List;

public interface Function 
{
	Value invoke(EvaluationContext context, List<Expression> parameters) 
		throws PolicyEvaluationException;
}
