package com.artagon.xacml.v30.spi.function;

import java.util.List;

import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionSpec;
import com.artagon.xacml.v30.pdp.ValueType;
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
			List<Expression> arguments) {
		return returnType;
	}
}
