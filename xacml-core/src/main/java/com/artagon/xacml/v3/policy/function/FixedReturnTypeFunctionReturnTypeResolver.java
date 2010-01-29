package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;

class FixedReturnTypeFunctionReturnTypeResolver implements FunctionReturnTypeResolver
{
	private ValueType returnType;
	
	FixedReturnTypeFunctionReturnTypeResolver(ValueType returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	@Override
	public ValueType resolve(FunctionSpec spec, Expression... arguments) {
		return returnType;
	}
	
}
