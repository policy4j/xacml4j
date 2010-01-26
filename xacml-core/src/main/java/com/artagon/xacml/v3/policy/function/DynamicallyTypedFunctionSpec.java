package com.artagon.xacml.v3.policy.function;

import java.util.List;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.BaseFunctionSpec;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.ParamSpec;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.ValueType;

final class DynamicallyTypedFunctionSpec extends BaseFunctionSpec
{
	private DynamicallyTypedFunction function;

	DynamicallyTypedFunctionSpec(String id, 
			DynamicallyTypedFunction function,
			List<ParamSpec> params) {
		super(id, params, false);
		Preconditions.checkNotNull(function);
		this.function = function;
	}
	
	@Override
	protected final ValueType getReturnType() {
		return null;
	}
	
	@Override
	protected ValueType resolveReturnType(Expression ...arguments) {
		return function.resolveReturnType(arguments);
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...arguments)
			throws EvaluationException {
		return function.invoke(context, isRequiresLazyParamEval()?arguments:evaluate(context, arguments));
	}	
}
