package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.policy.BaseFunctionSpec;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.ParamSpec;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.util.Preconditions;

final class StaticallyTypedFunctionSpec extends BaseFunctionSpec
{
	private StaticallyTypedFunction function;
	
	public StaticallyTypedFunctionSpec(String id, 
			StaticallyTypedFunction function, List<ParamSpec> params) {
		super(id, params);
		Preconditions.checkNotNull(function);
		this.function = function;
	}

	@Override
	protected final ValueType getReturnType() {
		return function.getReturnType();
	}
	
	@Override
	protected final ValueType resolveReturnType(Expression ...arguments){
		return function.getReturnType();
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...arguments)
			throws PolicyEvaluationException {
		return function.invoke(context, arguments);
	}
}
