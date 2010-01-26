package com.artagon.xacml.policy.function;

import java.util.List;

import com.artagon.xacml.policy.BaseFunctionSpec;
import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.ParamSpec;
import com.artagon.xacml.policy.EvaluationException;
import com.artagon.xacml.policy.EvaluationIndeterminateException;
import com.artagon.xacml.policy.Value;
import com.artagon.xacml.policy.ValueType;
import com.artagon.xacml.util.Preconditions;

final class StaticallyTypedFunctionSpec extends BaseFunctionSpec
{
	private StaticallyTypedFunction function;
	
	StaticallyTypedFunctionSpec(String id, 
			StaticallyTypedFunction function, 
			List<ParamSpec> params) {
		super(id, params, false);
		Preconditions.checkNotNull(function);
		this.function = function;
	}

	@Override
	protected final ValueType getReturnType() {
		return function.getReturnType();
	}
	
	/**
	 * Always returns the same value as {@link #getReturnType()}
	 * 
	 * @return {@link ValueType} instance
	 */
	@Override
	protected final ValueType resolveReturnType(Expression ...arguments){
		return function.getReturnType();
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...arguments)
			throws EvaluationException {
		if(context.isValidateFuncParamAtRuntime() &&
				!validateParameters(arguments)){
			throw new EvaluationIndeterminateException(
					"Failed to invoke function=\"%s\"", getXacmlId());
		}
		return function.invoke(context, isRequiresLazyParamEval()?arguments:evaluate(context, arguments));
	}
}
