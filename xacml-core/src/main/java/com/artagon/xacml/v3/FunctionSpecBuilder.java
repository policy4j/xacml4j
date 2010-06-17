package com.artagon.xacml.v3;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Preconditions;

public final class FunctionSpecBuilder 
{
	private String functionId;
	private String legacyId;
	private List<ParamSpec> paramSpec;
	private boolean hadVarArg = false;
	private boolean lazyArgumentEvaluation;
	
	public FunctionSpecBuilder(String functionId){
		this(functionId, null);
	}
	
	public FunctionSpecBuilder(String functionId, String legacyId){
		Preconditions.checkNotNull(functionId);
		this.functionId = functionId;
		this.legacyId = legacyId;
		this.paramSpec = new LinkedList<ParamSpec>();
	}
	
	public FunctionSpecBuilder withParam(ValueType type){
		Preconditions.checkNotNull(type);
		Preconditions.checkState(!hadVarArg, 
				String.format("Can't add parameter after variadic parameter"));
		this.paramSpec.add(new ParamValueTypeSpec(type));
		return this;
	}
	
	public FunctionSpecBuilder withLazyArgumentsEvaluation(){
		this.lazyArgumentEvaluation = true;
		return this;
	}
	
	public FunctionSpecBuilder withParam(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min > 1, "Max and min should be different at least by 1");
		hadVarArg = true;
		this.paramSpec.add(new ParamValueTypeSequenceSpec(min, max, type));
		return this;
	}
	
	public FunctionSpecBuilder withParamAnyBag() {
		this.paramSpec.add(new ParamAnyBagSpec());
		return this;
	}

	/**
	 * Builds {@link FunctionSpec} with a given
	 * {@link ValueType} as a function return type
	 * and given {@link FunctionInvocation} as a
	 * function implementation
	 * 
	 * @param returnType a function return type
	 * @param invocation a function
	 * @return {@link DefaultFunctionSpec} a f
	 */
	public DefaultFunctionSpec build(FunctionReturnTypeResolver returnType, FunctionInvocation invocation) {
		return new DefaultFunctionSpec(functionId, legacyId, paramSpec, returnType, invocation, lazyArgumentEvaluation);
	}

	public DefaultFunctionSpec build(ValueType returnType,
			FunctionInvocation invocation) {
		return build(
				new FixedReturnTypeFunctionReturnTypeResolver(returnType), 
				invocation);
	}
	
	
}
