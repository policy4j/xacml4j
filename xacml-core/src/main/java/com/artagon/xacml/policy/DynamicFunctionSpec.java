package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.util.Preconditions;

final class DynamicFunctionSpec extends BaseFunctionSpec
{
	private DynamicFunction function;

	public DynamicFunctionSpec(String id, DynamicFunction function,
			List<ParamSpec> params) {
		super(id, params);
		Preconditions.checkNotNull(function);
		this.function = function;
	}
	
	@Override
	protected ValueType getReturnType() {
		throw new UnsupportedOperationException(
				String.format(
						"Dynamic function=\"%s\" return type is not known staticaly", getId()));
	}
	
	@Override
	protected ValueType resolveReturnType(Expression ...arguments) {
		return function.resolveReturnType(arguments);
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...arguments)
			throws PolicyEvaluationException {
		return function.invoke(context, arguments);
	}
	
	
}
