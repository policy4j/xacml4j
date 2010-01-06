package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.util.Preconditions;

public class RegularFunctionSpec extends BaseFunctionSpec
{
	private ValueType returnType;
	
	public RegularFunctionSpec(FunctionId id, 
			ValueType returnType, FunctionImplementation function, List<ParamSpec> paramSpec) {
		super(id, function, paramSpec);
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}

	@Override
	public ValueType getReturnType(List<Expression> arguments) {
		return returnType;
	}
}
