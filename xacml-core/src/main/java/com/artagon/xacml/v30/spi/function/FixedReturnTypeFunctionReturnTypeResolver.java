package com.artagon.xacml.v30.spi.function;

import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.ValueType;
import com.google.common.base.Preconditions;

final class FixedReturnTypeFunctionReturnTypeResolver implements FunctionReturnTypeResolver
{
	private ValueType returnType;
	
	public FixedReturnTypeFunctionReturnTypeResolver(ValueType returnType){
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}
	
	@Override
	public ValueType resolve(FunctionSpec spec, 
			Expression... arguments) {
		return returnType;
	}
}
