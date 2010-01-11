package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.util.Preconditions;

public final class RegularFunctionSpec extends BaseFunctionSpec
{
	private ValueType returnType;
	
	public RegularFunctionSpec(FunctionId id, FunctionImplementation function,
			ValueType returnType,
			List<ParamSpec> params) {
		super(id, function, params);
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}

	@Override
	public boolean isHigherOrderFunction() {
		return false;
	}
	
	public ValueType getReturnType(){
		return returnType;
	}
	
}
