package com.artagon.xacml.v3.policy.function;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.BaseFunctionSpec;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ParamSpec;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.ValueType;

public class DefaultFunctionSpecBuilder 
	implements FunctionSpecBuilder
{
	private String functionId;
	private List<ParamSpec> paramSpec;
	private boolean hadVarArg = false;
	private boolean lazyArgumentEvaluation;
	
	public DefaultFunctionSpecBuilder(String functionId){
		Preconditions.checkNotNull(functionId);
		this.functionId = functionId;
		this.paramSpec = new LinkedList<ParamSpec>();
	}
	
	public DefaultFunctionSpecBuilder withParam(ValueType type){
		Preconditions.checkNotNull(type);
		Preconditions.checkState(!hadVarArg, 
				String.format("Can't add parameter after variadic parameter"));
		this.paramSpec.add(new ParamValueTypeSpec(type));
		return this;
	}
	
	public DefaultFunctionSpecBuilder withLazyArgumentsEvaluation(){
		this.lazyArgumentEvaluation = true;
		return this;
	}
	
	public DefaultFunctionSpecBuilder withParam(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min > 1, "Max and min should be different at least by 1");
		hadVarArg = true;
		this.paramSpec.add(new ParamValueTypeSequenceSpec(min, max, type));
		return this;
	}

	@Override
	public FunctionSpec build(final FunctionReturnTypeResolver returnType,
			final FunctionInvocation invocation) {
		return new BaseFunctionSpec(functionId, paramSpec, lazyArgumentEvaluation) {
			@Override
			public ValueType resolveReturnType(Expression... arguments) {
				return returnType.resolve(this, arguments);
			}
			@SuppressWarnings("unchecked")
			@Override
			protected <T extends Value> T doInvoke(EvaluationContext context,
					Expression... arguments) throws EvaluationException {
				return (T)invocation.invoke(this, context, arguments);
			}
		};
	}

	@Override
	public FunctionSpec build(final ValueType returnType,
			final FunctionInvocation invocation) {
		return new BaseFunctionSpec(functionId, paramSpec, lazyArgumentEvaluation) {
			@Override
			public ValueType resolveReturnType(Expression... arguments) {
				return returnType;
			}
			@SuppressWarnings("unchecked")
			@Override
			protected <T extends Value> T doInvoke(EvaluationContext context,
					Expression... params) throws EvaluationException {
				return (T)invocation.invoke(this, context, params);
			}
		};
	}
	
	
}
