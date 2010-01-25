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

final class DynamicallyTypedFunctionSpec extends BaseFunctionSpec
{
	private DynamicallyTypedFunction function;

	public DynamicallyTypedFunctionSpec(String id, DynamicallyTypedFunction function,
			List<ParamSpec> params) {
		super(id, params);
		Preconditions.checkNotNull(function);
		this.function = function;
	}
	
	@Override
	protected ValueType getReturnType() {
		throw new UnsupportedOperationException(
				String.format(
						"Dynamic function=\"%s\" return type is not known staticaly", getXacmlId()));
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
