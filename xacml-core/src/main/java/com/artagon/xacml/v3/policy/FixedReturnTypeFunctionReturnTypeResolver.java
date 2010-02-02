package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

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
