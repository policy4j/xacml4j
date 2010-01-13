package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.util.Preconditions;

final class RegularFunctionSpec extends BaseFunctionSpec
{
	private RegularFunction function;
	
	public RegularFunctionSpec(FunctionId id, 
			RegularFunction function, List<ParamSpec> params) {
		super(id, params);
		Preconditions.checkNotNull(function);
		this.function = function;
	}

	@Override
	protected final ValueType getReturnType() {
		return function.getReturnType();
	}
	
	@Override
	protected final ValueType resolveReturnType(List<Expression> arguments){
		return function.getReturnType();
	}

	@Override
	public Value invoke(EvaluationContext context, List<Expression> arguments)
			throws PolicyEvaluationException {
		return function.invoke(context, arguments);
	}
}
