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
	public <T extends Value> T invoke(EvaluationContext context, Expression ...arguments)
			throws EvaluationException {
		if(context.isValidateFuncParamAtRuntime() &&
				!validateParameters(arguments)){
			throw new EvaluationException(
					"Failed to invoke function=\"%s\"", getXacmlId());
		}
		return function.invoke(context, isRequiresLazyParamEval()?arguments:evaluate(context, arguments));
	}
}
