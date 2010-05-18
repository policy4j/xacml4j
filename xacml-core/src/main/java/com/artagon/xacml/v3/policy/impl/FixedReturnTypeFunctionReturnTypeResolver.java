package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionReturnTypeResolver;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ValueType;
import com.google.common.base.Preconditions;

final class FixedReturnTypeFunctionReturnTypeResolver implements FunctionReturnTypeResolver
{
	private ValueType returnType;
	
	FixedReturnTypeFunctionReturnTypeResolver(ValueType returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	@Override
	public ValueType resolve(FunctionSpec spec, 
			Expression... arguments) {
		return returnType;
	}
}
